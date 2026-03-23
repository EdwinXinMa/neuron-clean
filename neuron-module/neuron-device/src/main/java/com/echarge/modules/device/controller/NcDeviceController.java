package com.echarge.modules.device.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echarge.common.api.vo.Result;
import com.echarge.common.exception.NeuronBootException;
import com.echarge.common.util.RedisUtil;
import com.echarge.modules.alert.entity.NcAlert;
import com.echarge.modules.alert.service.INcAlertService;
import com.echarge.modules.device.entity.NcConnector;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.service.INcConnectorService;
import com.echarge.modules.device.service.INcDeviceService;
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
        return Result.OK(page);
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

        return Result.OK(result);
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
                .eq(NcDevice::getOnlineStatus, "ONLINE")));
        stats.put("offline", ncDeviceService.count(new LambdaQueryWrapper<NcDevice>()
                .eq(NcDevice::getDelFlag, 0).isNull(NcDevice::getParentDeviceId)
                .eq(NcDevice::getOnlineStatus, "OFFLINE")));
        stats.put("fault", ncDeviceService.count(new LambdaQueryWrapper<NcDevice>()
                .eq(NcDevice::getDelFlag, 0).isNull(NcDevice::getParentDeviceId)
                .eq(NcDevice::getOnlineStatus, "FAULT")));
        stats.put("unactivated", ncDeviceService.count(new LambdaQueryWrapper<NcDevice>()
                .eq(NcDevice::getDelFlag, 0).isNull(NcDevice::getParentDeviceId)
                .eq(NcDevice::getOnlineStatus, "UNACTIVATED")));
        stats.put("alertBadge", ncAlertService.countRecentCritical());

        return Result.OK(stats);
    }

    @Operation(summary = "地图设备坐标")
    @GetMapping("/mapdata")
    public Result<List<Map<String, Object>>> mapdata() {
        List<NcDevice> devices = ncDeviceService.list(
                new LambdaQueryWrapper<NcDevice>()
                        .eq(NcDevice::getDelFlag, 0)
                        .isNull(NcDevice::getParentDeviceId)
                        .isNotNull(NcDevice::getLat)
                        .select(NcDevice::getSn, NcDevice::getOnlineStatus,
                                NcDevice::getLat, NcDevice::getLng,
                                NcDevice::getDeviceModel)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (NcDevice d : devices) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sn", d.getSn());
            item.put("status", d.getOnlineStatus() == null ? "UNACTIVATED" : d.getOnlineStatus().toLowerCase());
            item.put("lat", d.getLat());
            item.put("lng", d.getLng());
            item.put("model", d.getDeviceModel());
            result.add(item);
        }
        return Result.OK(result);
    }

    @Operation(summary = "台账编辑")
    @PutMapping("/edit")
    public Result<?> edit(@RequestBody NcDevice device) {
        if (device.getId() == null) {
            return Result.error("缺少设备ID");
        }
        ncDeviceService.updateById(device);
        return Result.OK("修改成功");
    }

    @Operation(summary = "台账录入")
    @PostMapping("/add")
    public Result<?> add(@RequestBody NcDevice device) {
        try {
            ncDeviceService.register(device);
            return Result.OK("录入成功");
        } catch (NeuronBootException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "禁用设备")
    @PostMapping("/disable")
    public Result<?> disable(@RequestParam String id) {
        try {
            ncDeviceService.disable(id);
            return Result.OK("已禁用");
        } catch (NeuronBootException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "启用设备")
    @PostMapping("/enable")
    public Result<?> enable(@RequestParam String id) {
        try {
            ncDeviceService.enable(id);
            return Result.OK("已启用");
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

                device.setDeviceType("N3_LITE");
                device.setOnlineStatus("UNACTIVATED");
                device.setStatus("NORMAL");
                device.setDelFlag(0);

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
        return Result.OK(result);
    }

    private Map<String, String> buildError(int row, String sn, String reason) {
        Map<String, String> err = new LinkedHashMap<>();
        err.put("row", String.valueOf(row));
        err.put("sn", sn);
        err.put("reason", reason);
        return err;
    }
}
