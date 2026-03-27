package com.echarge.modules.device.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echarge.common.api.vo.Result;
import com.echarge.common.exception.NeuronBootException;
import com.echarge.common.ocpp.OcppCommandSender;
import com.echarge.common.system.vo.LoginUser;
import com.echarge.common.util.RedisUtil;
import com.echarge.modules.alert.entity.NcAlert;
import com.echarge.common.constant.BizConstant;
import com.echarge.modules.alert.service.INcAlertService;
import com.echarge.modules.device.entity.NcConnector;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.entity.NcOpLog;
import com.echarge.modules.device.service.INcConnectorService;
import com.echarge.modules.device.service.INcDeviceService;
import com.echarge.modules.device.service.impl.NcDeviceServiceImpl;
import com.echarge.modules.device.service.INcDlmHistoryService;
import com.echarge.modules.device.service.INcOpLogService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.shiro.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * @author Edwin
 */
@Slf4j
@Tag(name = "设备管理")
@RestController
@RequestMapping("/device")
public class NcDeviceController {

    @Autowired
    private INcDeviceService ncDeviceService;

    @Autowired
    private INcConnectorService ncConnectorService;

    @Autowired
    private INcAlertService ncAlertService;

    @Autowired
    private RedisUtil redisClient;

    @Autowired
    private OcppCommandSender ocppCommandSender;

    @Autowired
    private INcOpLogService opLogService;

    @Autowired
    private INcDlmHistoryService dlmHistoryService;

    @Operation(summary = "设备列表")
    @GetMapping("/list")
    public Result<IPage<NcDevice>> list(
            @RequestParam(required = false) String sn,
            @RequestParam(required = false) String onlineStatus,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        LambdaQueryWrapper<NcDevice> query = new LambdaQueryWrapper<>();
        query.eq(NcDevice::getDelFlag, 0);
        query.isNull(NcDevice::getParentDeviceId);

        if (StringUtils.isNotBlank(sn)) {
            query.like(NcDevice::getSn, sn);
        }
        if (StringUtils.isNotBlank(onlineStatus) && !"all".equals(onlineStatus)) {
            query.eq(NcDevice::getOnlineStatus, onlineStatus.toUpperCase());
        }
        // 在线 > 故障 > 离线 > 未激活，同状态内按最后心跳倒序
        query.last("ORDER BY CASE online_status WHEN 'ONLINE' THEN 0 WHEN 'FAULT' THEN 1 WHEN 'OFFLINE' THEN 2 WHEN 'UNACTIVATED' THEN 3 ELSE 4 END ASC, last_heartbeat DESC NULLS LAST");

        IPage<NcDevice> page = ncDeviceService.page(new Page<>(pageNo, pageSize), query);
        return Result.ok(page);
    }

    @Operation(summary = "设备详情（聚合）")
    @GetMapping("/{sn}/detail")
    public Result<Map<String, Object>> detail(@PathVariable String sn) {
        NcDevice device = ncDeviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn)
        );
        if (device == null) {
            return Result.error("设备不存在: " + sn);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("device", device);

        String redisKey = "device:dlm:" + sn;
        Object dlmRaw = redisClient.get(redisKey);
        Map<String, Object> ctData = new LinkedHashMap<>();
        if (dlmRaw != null) {
            try {
                JSONObject dlm = JSONObject.parseObject(dlmRaw.toString());
                ctData.put("totalCurrent", dlm.getDoubleValue("totalCurrent"));
                ctData.put("voltage", dlm.getDoubleValue("voltage"));
                ctData.put("totalPower", dlm.getDoubleValue("totalPower"));
                ctData.put("deviceTemp", dlm.getDoubleValue("deviceTemp"));
                ctData.put("wifiRssi", dlm.getIntValue("wifiRssi"));
                ctData.put("breakerRating", dlm.getIntValue("breakerRating"));
                ctData.put("pileAllocations", dlm.getJSONArray("pileAllocations"));
                ctData.put("dataFresh", true);
            } catch (Exception e) {
                log.warn("Failed to parse DLM Redis data for {}: {}", sn, e.getMessage());
                ctData.put("dataFresh", false);
                ctData.put("breakerRating", device.getBreakerRating());
            }
        } else {
            ctData.put("dataFresh", false);
            ctData.put("breakerRating", device.getBreakerRating());
            ctData.put("totalCurrent", null);
            ctData.put("voltage", null);
            ctData.put("totalPower", null);
        }
        result.put("ctData", ctData);

        List<NcDevice> childDevices = ncDeviceService.list(
                new LambdaQueryWrapper<NcDevice>()
                        .eq(NcDevice::getParentDeviceId, device.getId())
                        .eq(NcDevice::getDelFlag, 0)
        );
        List<Map<String, Object>> chargers = new ArrayList<>();
        for (NcDevice child : childDevices) {
            Map<String, Object> charger = new LinkedHashMap<>();
            charger.put("sn", child.getSn());
            charger.put("model", child.getDeviceModel());
            charger.put("onlineStatus", child.getOnlineStatus());
            List<NcConnector> connectors = ncConnectorService.list(
                    new LambdaQueryWrapper<NcConnector>().eq(NcConnector::getDeviceId, child.getId())
            );
            charger.put("connectors", connectors);
            chargers.add(charger);
        }
        result.put("chargers", chargers);

        Date since = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        List<NcAlert> alerts = ncAlertService.list(
                new LambdaQueryWrapper<NcAlert>()
                        .eq(NcAlert::getDeviceSn, sn)
                        .ge(NcAlert::getAlertTime, since)
                        .orderByDesc(NcAlert::getAlertTime)
                        .last("LIMIT 10")
        );
        result.put("recentAlerts", alerts);

        return Result.ok(result);
    }

    @Operation(summary = "设备统计（仪表盘）")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        LambdaQueryWrapper<NcDevice> base = new LambdaQueryWrapper<NcDevice>()
                .eq(NcDevice::getDelFlag, 0)
                .isNull(NcDevice::getParentDeviceId);

        stats.put("total", ncDeviceService.count(base));
        stats.put("online", ncDeviceService.count(new LambdaQueryWrapper<NcDevice>()
                .eq(NcDevice::getDelFlag, 0).isNull(NcDevice::getParentDeviceId)
                .eq(NcDevice::getOnlineStatus, BizConstant.DEVICE_ONLINE)));
        stats.put("offline", ncDeviceService.count(new LambdaQueryWrapper<NcDevice>()
                .eq(NcDevice::getDelFlag, 0).isNull(NcDevice::getParentDeviceId)
                .eq(NcDevice::getOnlineStatus, BizConstant.DEVICE_OFFLINE)));
        stats.put("fault", ncDeviceService.count(new LambdaQueryWrapper<NcDevice>()
                .eq(NcDevice::getDelFlag, 0).isNull(NcDevice::getParentDeviceId)
                .eq(NcDevice::getOnlineStatus, BizConstant.DEVICE_FAULT)));
        stats.put("unactivated", ncDeviceService.count(new LambdaQueryWrapper<NcDevice>()
                .eq(NcDevice::getDelFlag, 0).isNull(NcDevice::getParentDeviceId)
                .eq(NcDevice::getOnlineStatus, BizConstant.DEVICE_UNACTIVATED)));
        stats.put("alertBadge", ncAlertService.countRecentCritical());

        return Result.ok(stats);
    }

    @Operation(summary = "地图设备坐标")
    @GetMapping("/mapdata")
    public Result<List<Map<String, Object>>> mapdata() {
        List<NcDevice> devices = ncDeviceService.list(
                new LambdaQueryWrapper<NcDevice>()
                        .eq(NcDevice::getDelFlag, 0)
                        .isNull(NcDevice::getParentDeviceId)
                        .isNotNull(NcDevice::getLat)
                        .ne(NcDevice::getOnlineStatus, BizConstant.DEVICE_UNACTIVATED)
                        .select(NcDevice::getSn, NcDevice::getOnlineStatus,
                                NcDevice::getLat, NcDevice::getLng,
                                NcDevice::getDeviceModel)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (NcDevice d : devices) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sn", d.getSn());
            item.put("status", d.getOnlineStatus() == null ? BizConstant.DEVICE_UNACTIVATED : d.getOnlineStatus().toLowerCase());
            item.put("lat", d.getLat());
            item.put("lng", d.getLng());
            item.put("model", d.getDeviceModel());
            result.add(item);
        }
        return Result.ok(result);
    }

    @Operation(summary = "台账编辑")
    @PutMapping("/edit")
    public Result<?> edit(@RequestBody NcDevice device) {
        if (device.getId() == null) {
            return Result.error("缺少设备ID");
        }
        ncDeviceService.updateById(device);
        return Result.ok("修改成功");
    }

    @Operation(summary = "台账录入")
    @PostMapping("/add")
    public Result<?> add(@RequestBody NcDevice device) {
        try {
            ncDeviceService.register(device);
            return Result.ok("录入成功");
        } catch (NeuronBootException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "禁用设备")
    @PostMapping("/disable")
    public Result<?> disable(@RequestParam String id) {
        try {
            ncDeviceService.disable(id);
            return Result.ok("已禁用");
        } catch (NeuronBootException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "启用设备")
    @PostMapping("/enable")
    public Result<?> enable(@RequestParam String id) {
        try {
            ncDeviceService.enable(id);
            return Result.ok("已启用");
        } catch (NeuronBootException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "Excel批量导入")
    @PostMapping("/importExcel")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        int success = 0;
        int skipped = 0;
        List<Map<String, String>> errors = new ArrayList<>();

        try {
            ImportParams importParams = new ImportParams();
            importParams.setTitleRows(0);
            importParams.setHeadRows(1);

            List<NcDevice> list = ExcelImportUtil.importExcel(file.getInputStream(), NcDevice.class, importParams);

            for (int i = 0; i < list.size(); i++) {
                NcDevice device = list.get(i);
                int row = i + 2;

                String sn = device.getSn();
                if (StringUtils.isNotBlank(sn)) {
                    sn = sn.trim().replaceAll("\\s+", "").toUpperCase();
                    device.setSn(sn);
                }

                if (StringUtils.isBlank(device.getSn())) {
                    errors.add(buildError(row, device.getSn(), "SN为空"));
                    continue;
                }
                if (StringUtils.isBlank(device.getDealer())) {
                    errors.add(buildError(row, device.getSn(), "经销商为空"));
                    continue;
                }
                if (device.getShipDate() == null) {
                    errors.add(buildError(row, device.getSn(), "出货日期为空"));
                    continue;
                }

                if (ncDeviceService.existsBySn(device.getSn())) {
                    skipped++;
                    continue;
                }

                device.setDeviceType(BizConstant.TYPE_N3_LITE);
                device.setOnlineStatus(BizConstant.DEVICE_UNACTIVATED);
                device.setStatus("NORMAL");
                device.setDelFlag(0);
                NcDeviceServiceImpl.assignRandomLocation(device);

                ncDeviceService.save(device);
                success++;
            }
        } catch (Exception e) {
            log.error("Excel导入异常", e);
            return Result.error("导入失败: " + e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        result.put("skipped", skipped);
        result.put("errors", errors);
        return Result.ok(result);
    }

    /**
     * DLM 修改（修改断路器额定电流）
     * 通过 OCPP DataTransfer 下发配置到设备，同时更新数据库
     */
    @Operation(summary = "DLM 修改")
    @PostMapping("/{sn}/dlm")
    public Result<?> updateDlm(@PathVariable String sn, @RequestBody JSONObject params) {
        Integer breakerRating = params.getInteger("breakerRating");
        if (breakerRating == null || breakerRating <= 0) {
            return Result.error("breakerRating 参数无效");
        }

        NcDevice device = ncDeviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn)
        );
        if (device == null) {
            return Result.error("设备不存在: " + sn);
        }

        Integer oldRating = device.getBreakerRating();

        // 更新数据库
        device.setBreakerRating(breakerRating);
        ncDeviceService.updateById(device);

        // 同步更新 Redis 中的 DLM 数据
        String redisKey = "device:dlm:" + sn;
        Object dlmRaw = redisClient.get(redisKey);
        if (dlmRaw != null) {
            try {
                JSONObject dlm = JSONObject.parseObject(dlmRaw.toString());
                dlm.put("breakerRating", breakerRating);
                redisClient.set(redisKey, dlm.toJSONString(), 300);
            } catch (Exception e) {
                log.warn("更新 Redis DLM 数据失败: {}", e.getMessage());
            }
        }

        // 如果设备在线，通过 OCPP 下发配置
        if (ocppCommandSender.isDeviceConnected(sn)) {
            String messageId = "dlm-" + java.util.UUID.randomUUID().toString().substring(0, 8);
            JsonObject payload = new JsonObject();
            payload.addProperty("breakerRating", breakerRating);

            JsonArray call = new JsonArray();
            call.add(2);
            call.add(messageId);
            call.add("DataTransfer");

            JsonObject dtPayload = new JsonObject();
            dtPayload.addProperty("vendorId", "AlwaysControl");
            dtPayload.addProperty("messageId", BizConstant.DT_SET_DLM_CONFIG);
            dtPayload.addProperty("data", payload.toString());
            call.add(dtPayload);

            ocppCommandSender.sendCall(sn, call.toString());
            log.info("[DLM] Config sent to {}: breakerRating={}A", sn, breakerRating);
        }

        // 记录操作日志
        String opUser = "system";
        try {
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (loginUser != null) {
                opUser = loginUser.getUsername();
            }
        } catch (Exception ignored) {}

        NcOpLog opLog = new NcOpLog();
        opLog.setDeviceSn(sn);
        opLog.setOpUser(opUser);
        opLog.setOpType(NcOpLog.DLM_CONFIG);
        opLog.setOpContent((oldRating != null ? oldRating : "?") + "A → " + breakerRating + "A");
        opLog.setOpResult(NcOpLog.SUCCESS);
        opLog.setOpTime(new Date());
        opLog.setCreateTime(new Date());
        opLogService.save(opLog);

        return Result.ok("DLM 配置已更新");
    }

    /**
     * DLM 历史图表数据
     * 按时间范围聚合，返回断路器额定值、总电流、充电电流、家庭电流等
     */
    @Operation(summary = "DLM 历史图表")
    @GetMapping("/{sn}/dlm/history")
    public Result<Map<String, Object>> dlmHistory(
            @PathVariable String sn,
            @RequestParam(defaultValue = "24h") String range) {
        NcDevice device = ncDeviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn)
        );
        if (device == null) {
            return Result.error("设备不存在: " + sn);
        }
        Map<String, Object> chartData = dlmHistoryService.getChartData(sn, range);
        return Result.ok(chartData);
    }

    /**
     * 远程重启设备
     * 通过 OCPP Reset 命令下发到设备
     */
    @Operation(summary = "远程重启")
    @PostMapping("/{sn}/reset")
    public Result<?> resetDevice(@PathVariable String sn,
                                 @RequestParam(defaultValue = "Soft") String type) {
        NcDevice device = ncDeviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn)
        );
        if (device == null) {
            return Result.error("设备不存在: " + sn);
        }
        if (!ocppCommandSender.isDeviceConnected(sn)) {
            return Result.error("设备离线，无法下发重启命令");
        }

        // 构建 OCPP CALL: [2, messageId, "Reset", {"type": "Soft"}]
        String messageId = "reset-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        JsonArray call = new JsonArray();
        call.add(2);
        call.add(messageId);
        call.add("Reset");
        JsonObject payload = new JsonObject();
        payload.addProperty("type", type);
        call.add(payload);

        ocppCommandSender.sendCall(sn, call.toString());
        log.info("[Reset] Command sent to {}: type={}", sn, type);

        // 记录操作日志
        String opUser = "system";
        try {
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (loginUser != null) {
                opUser = loginUser.getUsername();
            }
        } catch (Exception ignored) {}

        NcOpLog opLog = new NcOpLog();
        opLog.setDeviceSn(sn);
        opLog.setOpUser(opUser);
        opLog.setOpType(NcOpLog.REMOTE_REBOOT);
        opLog.setOpContent("远程重启（" + type + "）");
        opLog.setOpResult(NcOpLog.SUCCESS);
        opLog.setOpTime(new Date());
        opLog.setCreateTime(new Date());
        opLogService.save(opLog);

        return Result.ok("重启命令已下发");
    }

    private Map<String, String> buildError(int row, String sn, String reason) {
        Map<String, String> err = new LinkedHashMap<>();
        err.put("row", String.valueOf(row));
        err.put("sn", sn);
        err.put("reason", reason);
        return err;
    }
}
