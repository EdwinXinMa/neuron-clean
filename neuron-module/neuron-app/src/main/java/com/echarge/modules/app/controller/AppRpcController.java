package com.echarge.modules.app.controller;

import com.alibaba.fastjson.JSONObject;
import com.echarge.common.util.CommonUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echarge.modules.app.entity.AppUser;
import com.echarge.modules.app.entity.AppUserDevice;
import com.echarge.modules.app.mapper.AppUserDeviceMapper;
import com.echarge.common.ocpp.OcppCommandSender;
import com.echarge.modules.device.entity.FirmwareLatest;
import com.echarge.modules.device.entity.FirmwareVersion;
import com.echarge.modules.device.entity.NcChargingSession;
import com.echarge.modules.device.entity.NcConnector;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.entity.FirmwareUpgradeTask;
import com.echarge.modules.device.mapper.NcChargingSessionMapper;
import com.echarge.modules.device.service.IFirmwareUpgradeTaskService;
import com.echarge.modules.device.service.IFirmwareVersionService;
import com.echarge.modules.device.service.INcConnectorService;
import com.echarge.modules.device.service.INcDeviceService;
import com.echarge.modules.device.service.impl.FirmwareVersionServiceImpl;
import com.echarge.modules.device.websocket.AppWebSocket;
import com.echarge.common.constant.BizConstant;
import com.echarge.common.util.MinioUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.*;

/**
 * App RPC 统一入口 — 兼容本地 RPC 响应格式
 * 根据 method 字段路由到对应处理逻辑
 * @author Edwin
 */
@Slf4j
@RestController
@RequestMapping("/app")
@Tag(name = "App RPC 接口")
public class AppRpcController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private INcDeviceService deviceService;

    @Autowired
    private AppUserDeviceMapper userDeviceMapper;

    @Autowired
    private NcChargingSessionMapper chargingSessionMapper;

    @Autowired
    private INcConnectorService connectorService;

    @Autowired
    private OcppCommandSender ocppCommandSender;

    @Autowired
    private IFirmwareVersionService firmwareVersionService;

    @Autowired
    private IFirmwareUpgradeTaskService upgradeTaskService;

    /**
     * RPC 统一入口
     */
    @PostMapping("/rpccall")
    @Operation(summary = "App RPC 统一入口（兼容本地 RPC 格式）")
    public Map<String, Object> rpccall(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String method = (String) body.get("method");
        String deviceSn = (String) body.get("deviceSn");
        Map<String, Object> data = body.get("data") != null ? (Map<String, Object>) body.get("data") : new HashMap<>();

        // 校验设备归属
        AppUser user = (AppUser) request.getAttribute("appUser");
        if (deviceSn != null) {
            long bound = userDeviceMapper.selectCount(
                    new LambdaQueryWrapper<AppUserDevice>()
                            .eq(AppUserDevice::getUserId, user.getId())
                            .eq(AppUserDevice::getDeviceSn, deviceSn));
            if (bound == 0) {
                return rpcError(method, 403, "该设备未绑定到当前用户");
            }
        }

        // 按 method 路由
        if (method == null) {
            return rpcError(null, 400, "method 不能为空");
        }

        return switch (method) {
            case "SubDeviceManager.SelectSubDeviceByPortName" -> handleSelectSubDevices(method, deviceSn);
            case "SubDeviceManager.GetChargingStationsInfo" -> handleGetChargingStationsInfo(method, deviceSn);
            case "SubDeviceManager.SelectChargingDetails" -> handleSelectChargingDetails(method, deviceSn, data);
            case "SubDeviceManager.SelectChargingLoadCurrent" -> handleSelectChargingLoadCurrent(method, deviceSn, data);
            case "SubDeviceManager.SelectChargingLoadCurrentList" -> handleSelectChargingLoadCurrentList(method, deviceSn);
            case "SubDeviceManager.SelectChargingHistory" -> handleSelectChargingHistory(method, deviceSn, data);
            case "ConfigManager.GetConfig" -> handleGetConfig(method, deviceSn, data);
            case "ConfigManager.SetConfig" -> handleSetConfig(method, deviceSn, data, request);
            case "SubDeviceManager.StartChargingRequest" -> handleStartCharging(method, deviceSn, data);
            case "SubDeviceManager.StopChargingRequest" -> handleStopCharging(method, deviceSn, data);
            case "SubDeviceManager.RequestFirmwareUpdate" -> handleRequestFirmwareUpdate(method, deviceSn);
            case "SubDeviceManager.GetChargingWorkMode" -> handleGetWorkMode(method, deviceSn);
            case "SubDeviceManager.SetChargingStationWorkMode",
                 "SubDeviceManager.SetChargingWorkMode" -> handleSetWorkMode(method, deviceSn, data);
            // 云模式不需要的接口
            case "SubDeviceManager.SearchChargeStationRequest",
                 "SubDeviceManager.UpdateMajorSubDeviceByPortName" ->
                    rpcError(method, 400, "该功能仅在本地模式下可用");
            default -> rpcError(method, 400, "不支持的 method: " + method);
        };
    }

    // ═══════════════════════════════════════════════
    // 接口实现
    // ═══════════════════════════════════════════════

    /**
     * 查询已添加的充电桩（对应本地接口 #2）
     */
    private Map<String, Object> handleSelectSubDevices(String method, String deviceSn) {
        JSONObject dlm = getDlmData(deviceSn);
        List<Map<String, Object>> deviceList = new ArrayList<>();

        if (dlm != null && dlm.containsKey("pileAllocations")) {
            for (Object obj : dlm.getJSONArray("pileAllocations")) {
                JSONObject pile = (JSONObject) obj;
                Map<String, Object> item = new LinkedHashMap<>();
                String pileSn = pile.getString("sn");
                item.put("subDevId", pileSn);
                item.put("name", "充电桩");
                item.put("mdcFwVersion", pile.getString("charge_version"));
                item.put("connectStatus", pile.getString("connectStatus"));
                item.put("mac", pileSn);
                item.put("EVStatus", pile.getString("charge_EVStatus"));
                deviceList.add(item);
            }
        } else {
            // 回退到数据库查子设备
            List<NcDevice> children = deviceService.list(
                    new LambdaQueryWrapper<NcDevice>()
                            .eq(NcDevice::getParentDeviceId, getDeviceId(deviceSn))
                            .orderByAsc(NcDevice::getSn));
            for (NcDevice child : children) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("subDevId", child.getSn());
                item.put("name", child.getDeviceModel() != null ? child.getDeviceModel() : "充电桩");
                item.put("mdcFwVersion", child.getFirmwareVersion());
                item.put("connectStatus", "ONLINE".equals(child.getOnlineStatus()) ? "online" : "offline");
                item.put("mac", child.getSn());
                item.put("EVStatus", "Available");
                deviceList.add(item);
            }
        }

        return rpcSuccess(method, deviceSn, Map.of("deviceList", deviceList));
    }

    /**
     * 获取充电桩基本信息（对应本地接口 #17）
     */
    private Map<String, Object> handleGetChargingStationsInfo(String method, String deviceSn) {
        JSONObject dlm = getDlmData(deviceSn);
        List<Map<String, Object>> stations = new ArrayList<>();

        if (dlm != null && dlm.containsKey("pileAllocations")) {
            for (Object obj : dlm.getJSONArray("pileAllocations")) {
                JSONObject pile = (JSONObject) obj;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("mac", pile.getString("sn"));
                item.put("name", "充电桩");
                item.put("status", pile.getString("charge_EVStatus"));
                item.put("connectStatus", pile.getString("connectStatus"));
                stations.add(item);
            }
        } else {
            List<NcDevice> children = deviceService.list(
                    new LambdaQueryWrapper<NcDevice>()
                            .eq(NcDevice::getParentDeviceId, getDeviceId(deviceSn))
                            .orderByAsc(NcDevice::getSn));
            for (NcDevice child : children) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("mac", child.getSn());
                item.put("name", child.getDeviceModel() != null ? child.getDeviceModel() : "充电桩");
                item.put("status", "Unavailable");
                item.put("connectStatus", "offline");
                stations.add(item);
            }
        }

        return rpcSuccess(method, deviceSn, Map.of("chargingStations", stations));
    }

    /**
     * 查询充电明细（对应本地接口 #7）
     */
    private Map<String, Object> handleSelectChargingDetails(String method, String deviceSn, Map<String, Object> data) {
        String mac = getMac(data);
        JSONObject dlm = getDlmData(deviceSn);
        JSONObject pile = findPile(dlm, mac);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("subDevId", mac);
        result.put("name", "充电桩");
        result.put("mac", mac);

        // 先查充电中的会话
        NcChargingSession session = chargingSessionMapper.selectOne(
                new LambdaQueryWrapper<NcChargingSession>()
                        .eq(NcChargingSession::getDeviceSn, deviceSn)
                        .eq(NcChargingSession::getPileSn, mac)
                        .eq(NcChargingSession::getStatus, NcChargingSession.CHARGING)
                        .orderByDesc(NcChargingSession::getStartTime)
                        .last("LIMIT 1"));

        if (session != null) {
            // 充电中：返回实时数据
            result.put("startTime", String.valueOf(session.getStartTime().getTime()));
            result.put("endTime", "0");
            result.put("EVStatus", "Charging");
            result.put("energy", CommonUtils.whToKwh(session.getEnergy()));
            result.put("duration", String.valueOf(session.getDuration() != null ? session.getDuration() : 0));
            result.put("chargingMethod", session.getChargingMethod() != null ? session.getChargingMethod() : 0);
            result.put("isPlugged", 1);
        } else {
            // 查最近一条已完成的会话（5分钟内结束的），用于结单页面
            NcChargingSession finished = chargingSessionMapper.selectOne(
                    new LambdaQueryWrapper<NcChargingSession>()
                            .eq(NcChargingSession::getDeviceSn, deviceSn)
                            .eq(NcChargingSession::getPileSn, mac)
                            .eq(NcChargingSession::getStatus, NcChargingSession.FINISHED)
                            .ge(NcChargingSession::getEndTime, new Date(System.currentTimeMillis() - 5 * 60 * 1000))
                            .orderByDesc(NcChargingSession::getEndTime)
                            .last("LIMIT 1"));

            if (finished != null) {
                // 刚结束的充电：返回结单数据
                result.put("startTime", String.valueOf(finished.getStartTime().getTime()));
                result.put("endTime", String.valueOf(finished.getEndTime().getTime()));
                String finishedEvStatus = pile != null ? pile.getString("charge_EVStatus") : "Finishing";
                result.put("EVStatus", finishedEvStatus);
                result.put("energy", CommonUtils.whToKwh(finished.getEnergy()));
                result.put("duration", String.valueOf(finished.getDuration() != null ? finished.getDuration() : 0));
                result.put("chargingMethod", finished.getChargingMethod() != null ? finished.getChargingMethod() : 0);
                String finishEvStatus = pile != null ? pile.getString("charge_EVStatus") : "Available";
                result.put("isPlugged", (!"Available".equals(finishEvStatus) && !"Unavailable".equals(finishEvStatus)) ? 1 : 0);
            } else {
                // 空闲状态
                String evStatus = pile != null ? pile.getString("charge_EVStatus") : "Available";
                result.put("startTime", "0");
                result.put("endTime", "0");
                result.put("EVStatus", evStatus);
                result.put("energy", "0");
                result.put("duration", "0");
                result.put("chargingMethod", 0);
                result.put("isPlugged", (!"Available".equals(evStatus) && !"Unavailable".equals(evStatus)) ? 1 : 0);
            }
        }

        return rpcSuccess(method, deviceSn, result);
    }

    /**
     * 查询充电桩负载电流（对应本地接口 #10）
     */
    private Map<String, Object> handleSelectChargingLoadCurrent(String method, String deviceSn, Map<String, Object> data) {
        String mac = getMac(data);
        JSONObject dlm = getDlmData(deviceSn);
        JSONObject pile = findPile(dlm, mac);

        Map<String, Object> deviceInfo = new LinkedHashMap<>();
        deviceInfo.put("subDevId", mac);
        deviceInfo.put("mac", mac);
        deviceInfo.put("ChargingCurrent", pile != null ? pile.getString("allocatedCurrent") : "0");
        deviceInfo.put("LoadCurrent", dlm != null ? String.valueOf(
                dlm.getDoubleValue("loadCurrentA") + dlm.getDoubleValue("loadCurrentB") + dlm.getDoubleValue("loadCurrentC")) : "0");
        deviceInfo.put("MeterCurrent", dlm != null ? String.valueOf(
                dlm.getDoubleValue("totalCurrentA") + dlm.getDoubleValue("totalCurrentB") + dlm.getDoubleValue("totalCurrentC")) : "0");
        deviceInfo.put("energy", pile != null ? CommonUtils.whToKwh(pile.getIntValue("energy")) : "0");

        return rpcSuccess(method, deviceSn, Map.of("deviceInfo", deviceInfo));
    }

    /**
     * 查询总电流负载明细（对应本地接口 #12）
     */
    private Map<String, Object> handleSelectChargingLoadCurrentList(String method, String deviceSn) {
        JSONObject dlm = getDlmData(deviceSn);

        Map<String, Object> result = new LinkedHashMap<>();
        double totalA = dlm != null ? dlm.getDoubleValue("totalCurrentA") : 0;
        double totalB = dlm != null ? dlm.getDoubleValue("totalCurrentB") : 0;
        double totalC = dlm != null ? dlm.getDoubleValue("totalCurrentC") : 0;

        result.put("LoadCurrent", dlm != null ? String.valueOf(
                dlm.getDoubleValue("loadCurrentA") + dlm.getDoubleValue("loadCurrentB") + dlm.getDoubleValue("loadCurrentC")) : "0");
        result.put("MeterCurrent", String.valueOf(totalA + totalB + totalC));
        result.put("totalChargingCurrent", dlm != null ? String.valueOf(
                dlm.getDoubleValue("totalChargingCurrentA") + dlm.getDoubleValue("totalChargingCurrentB") + dlm.getDoubleValue("totalChargingCurrentC")) : "0");

        List<Map<String, Object>> deviceList = new ArrayList<>();
        if (dlm != null && dlm.containsKey("pileAllocations")) {
            for (Object obj : dlm.getJSONArray("pileAllocations")) {
                JSONObject pile = (JSONObject) obj;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("subDevId", pile.getString("sn"));
                item.put("mac", pile.getString("sn"));
                String rawAllocated = pile.getString("allocatedCurrent");
                log.info("[DLM] allocatedCurrent raw — 桩={}, 原始值={}, 类型={}", pile.getString("sn"), rawAllocated,
                        pile.get("allocatedCurrent") != null ? pile.get("allocatedCurrent").getClass().getSimpleName() : "null");
                item.put("ChargingCurrent", rawAllocated);
                item.put("energy", CommonUtils.whToKwh(pile.getIntValue("energy")));
                item.put("EVStatus", pile.getString("charge_EVStatus"));
                item.put("connectStatus", pile.getString("connectStatus"));
                deviceList.add(item);
            }
        } else {
            // N3 Lite 离线时回退到数据库查子设备
            List<NcDevice> children = deviceService.list(
                    new LambdaQueryWrapper<NcDevice>()
                            .eq(NcDevice::getParentDeviceId, getDeviceId(deviceSn))
                            .orderByAsc(NcDevice::getSn));
            for (NcDevice child : children) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("subDevId", child.getSn());
                item.put("mac", child.getSn());
                item.put("ChargingCurrent", "0");
                item.put("energy", "0.0");
                item.put("EVStatus", "Unavailable");
                item.put("connectStatus", "offline");
                deviceList.add(item);
            }
        }
        result.put("deviceList", deviceList);

        return rpcSuccess(method, deviceSn, result);
    }

    /**
     * 查询充电历史（对应本地接口 #16）
     */
    private Map<String, Object> handleSelectChargingHistory(String method, String deviceSn, Map<String, Object> data) {
        int page = data.get("page") != null ? ((Number) data.get("page")).intValue() : 0;
        int pageSize = 10;
        Integer year = data.get("year") != null ? ((Number) data.get("year")).intValue() : null;
        Integer month = data.get("month") != null ? ((Number) data.get("month")).intValue() : null;

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date startOfRange = null;
        java.util.Date endOfRange = null;
        if (year != null && month != null) {
            cal.set(year, month - 1, 1, 0, 0, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            startOfRange = cal.getTime();
            cal.add(java.util.Calendar.MONTH, 1);
            endOfRange = cal.getTime();
        } else if (year != null) {
            cal.set(year, 0, 1, 0, 0, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            startOfRange = cal.getTime();
            cal.add(java.util.Calendar.YEAR, 1);
            endOfRange = cal.getTime();
        }

        Page<NcChargingSession> result = chargingSessionMapper.selectPage(
                new Page<>(page + 1, pageSize),
                new LambdaQueryWrapper<NcChargingSession>()
                        .eq(NcChargingSession::getDeviceSn, deviceSn)
                        .eq(NcChargingSession::getStatus, NcChargingSession.FINISHED)
                        .ge(startOfRange != null, NcChargingSession::getStartTime, startOfRange)
                        .lt(endOfRange != null, NcChargingSession::getStartTime, endOfRange)
                        .orderByDesc(NcChargingSession::getStartTime));

        List<Map<String, Object>> historyList = new ArrayList<>();
        for (NcChargingSession s : result.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId());
            item.put("startTime", s.getStartTime() != null ? String.valueOf(s.getStartTime().getTime() / 1000) : "");
            item.put("endTime", s.getEndTime() != null ? String.valueOf(s.getEndTime().getTime() / 1000) : "");
            item.put("energy", CommonUtils.whToKwh(s.getEnergy()));
            item.put("duration", String.valueOf(s.getDuration() != null ? s.getDuration() : 0));
            item.put("subDevId", s.getPileSn());
            historyList.add(item);
        }

        Map<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("ChargingHistoryList", historyList);
        responseData.put("TotalPages", (int) result.getPages());
        responseData.put("CurrentPage", page);

        return rpcSuccess(method, deviceSn, responseData);
    }

    /**
     * 获取配置（对应本地接口 #8、#11）
     */
    private Map<String, Object> handleGetConfig(String method, String deviceSn, Map<String, Object> data) {
        String configname = (String) data.get("configname");

        if ("InflowMaxCurrent".equals(configname)) {
            JSONObject dlm = getDlmData(deviceSn);
            String value = "32";
            if (dlm != null && dlm.containsKey("breakerRating")) {
                value = dlm.getString("breakerRating");
            } else {
                NcDevice device = deviceService.getOne(
                        new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, deviceSn));
                if (device != null && device.getBreakerRating() != null) {
                    value = String.valueOf(device.getBreakerRating());
                }
            }
            return rpcSuccess(method, deviceSn, Map.of("configname", configname, "InflowMaxCurrent", value));
        }

        if ("version".equals(configname)) {
            NcDevice device = deviceService.getOne(
                    new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, deviceSn));
            String version = device != null && device.getFirmwareVersion() != null ? device.getFirmwareVersion() : "unknown";
            return rpcSuccess(method, deviceSn, Map.of("configname", configname, "version", version));
        }

        return rpcError(method, 400, "不支持的 configname: " + configname);
    }

    /**
     * 设置配置 — InflowMaxCurrent（对应本地接口 #9）
     */
    private Map<String, Object> handleSetConfig(String method, String deviceSn, Map<String, Object> data, HttpServletRequest request) {
        String configname = (String) data.get("configname");

        if (!"InflowMaxCurrent".equals(configname)) {
            return rpcError(method, 400, "云模式仅支持设置 InflowMaxCurrent");
        }

        Object val = data.get("InflowMaxCurrent");
        if (val == null) {
            return rpcError(method, 400, "InflowMaxCurrent 不能为空");
        }
        int breakerRating = ((Number) val).intValue();

        try {
            AppUser user = (AppUser) request.getAttribute("appUser");
            String opUser = user != null ? user.getEmail() : "app_user";
            deviceService.sendDlmConfig(deviceSn, breakerRating, opUser);
        } catch (Exception e) {
            return rpcError(method, 500, e.getMessage());
        }

        return rpcSuccess(method, deviceSn, Map.of("configname", configname));
    }

    // ═══════════════════════════════════════════════
    // 远程启停充电
    // ═══════════════════════════════════════════════

    /**
     * 启动充电 — OCPP RemoteStartTransaction
     */
    private Map<String, Object> handleStartCharging(String method, String deviceSn, Map<String, Object> data) {
        String mac = getMac(data);
        if (mac == null) {
            return rpcError(method, 400, "mac 不能为空");
        }

        // 检查设备在线
        if (!ocppCommandSender.isDeviceConnected(deviceSn)) {
            return rpcError(method, 400, "设备不在线，无法发起充电");
        }

        // 通过桩 SN 查第一把枪的 connectorId
        NcDevice pile = deviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, mac));
        if (pile == null) {
            return rpcError(method, 400, "未找到桩: " + mac);
        }

        NcConnector connector = connectorService.getOne(
                new LambdaQueryWrapper<NcConnector>()
                        .eq(NcConnector::getDeviceId, pile.getId())
                        .orderByAsc(NcConnector::getConnectorId)
                        .last("LIMIT 1"));
        if (connector == null) {
            return rpcError(method, 400, "该桩未上报枪信息，请等待设备上线后重试");
        }

        // 构建 OCPP RemoteStartTransaction
        log.info("[充电] App请求启动充电 — 设备={}, 桩={}, 枪={}", deviceSn, mac, connector.getConnectorId());
        String messageId = "rs-" + UUID.randomUUID().toString().substring(0, 8);
        JsonObject payload = new JsonObject();
        payload.addProperty("connectorId", connector.getConnectorId());
        payload.addProperty("idTag", "app_remote");

        JsonArray call = new JsonArray();
        call.add(2);
        call.add(messageId);
        call.add("RemoteStartTransaction");
        call.add(payload);

        // 发送并等待设备响应（10秒超时）
        String response = ocppCommandSender.sendCallAndWait(deviceSn, call.toString(), messageId, 10);
        if (response == null) {
            log.warn("[充电] 启动充电超时 — 设备={}, 桩={}, 设备未响应", deviceSn, mac);
            return rpcError(method, 504, "设备响应超时");
        }

        // 解析响应
        JsonObject respObj = JsonParser.parseString(response).getAsJsonObject();
        String status = respObj.has("status") ? respObj.get("status").getAsString() : "Rejected";
        if (!"Accepted".equals(status)) {
            log.warn("[充电] 启动充电被拒 — 设备={}, 桩={}, 响应={}", deviceSn, mac, status);
            return rpcError(method, 400, "设备拒绝启动充电（" + status + "）");
        }

        log.info("[充电] 启动充电成功 — 设备={}, 桩={}, 枪={}, 设备回复=Accepted", deviceSn, mac, connector.getConnectorId());
        return rpcSuccess(method, deviceSn, Map.of());
    }

    /**
     * 停止充电 — OCPP RemoteStopTransaction
     */
    private Map<String, Object> handleStopCharging(String method, String deviceSn, Map<String, Object> data) {
        String mac = getMac(data);
        if (mac == null) {
            return rpcError(method, 400, "mac 不能为空");
        }

        // 检查设备在线
        if (!ocppCommandSender.isDeviceConnected(deviceSn)) {
            return rpcError(method, 400, "设备不在线，无法停止充电");
        }

        // 查找该桩正在充电的会话，取 transactionId
        NcChargingSession session = chargingSessionMapper.selectOne(
                new LambdaQueryWrapper<NcChargingSession>()
                        .eq(NcChargingSession::getDeviceSn, deviceSn)
                        .eq(NcChargingSession::getPileSn, mac)
                        .eq(NcChargingSession::getStatus, NcChargingSession.CHARGING)
                        .orderByDesc(NcChargingSession::getStartTime)
                        .last("LIMIT 1"));
        if (session == null || session.getTransactionId() == null) {
            return rpcError(method, 400, "该桩当前没有进行中的充电会话");
        }

        // 构建 OCPP RemoteStopTransaction
        log.info("[充电] App请求停止充电 — 设备={}, 桩={}, txId={}", deviceSn, mac, session.getTransactionId());
        String messageId = "rp-" + UUID.randomUUID().toString().substring(0, 8);
        JsonObject payload = new JsonObject();
        payload.addProperty("transactionId", session.getTransactionId());

        JsonArray call = new JsonArray();
        call.add(2);
        call.add(messageId);
        call.add("RemoteStopTransaction");
        call.add(payload);

        // 发送并等待设备响应（10秒超时）
        String response = ocppCommandSender.sendCallAndWait(deviceSn, call.toString(), messageId, 10);
        if (response == null) {
            log.warn("[充电] 停止充电超时 — 设备={}, 桩={}, txId={}, 设备未响应", deviceSn, mac, session.getTransactionId());
            return rpcError(method, 504, "设备响应超时");
        }

        // 解析响应
        JsonObject respObj = JsonParser.parseString(response).getAsJsonObject();
        String status = respObj.has("status") ? respObj.get("status").getAsString() : "Rejected";
        if (!"Accepted".equals(status)) {
            log.warn("[充电] 停止充电被拒 — 设备={}, 桩={}, txId={}, 响应={}", deviceSn, mac, session.getTransactionId(), status);
            return rpcError(method, 400, "设备拒绝停止充电（" + status + "）");
        }

        log.info("[充电] 停止充电指令已接受 — 设备={}, 桩={}, txId={}, 等待设备StopTransaction...", deviceSn, mac, session.getTransactionId());

        // 等待 StopTransaction 处理完成，会话变为 FINISHED 后再返回 App（最多等 10 秒）
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            NcChargingSession updated = chargingSessionMapper.selectById(session.getId());
            if (updated != null && NcChargingSession.FINISHED.equals(updated.getStatus())) {
                log.info("[充电] 订单已结束 — 设备={}, 桩={}, txId={}, 等待{}秒", deviceSn, mac, session.getTransactionId(), i + 1);
                break;
            }
        }

        return rpcSuccess(method, deviceSn, Map.of());
    }

    // ═══════════════════════════════════════════════
    // 工作模式
    // ═══════════════════════════════════════════════

    /**
     * 获取充电桩工作模式 — 从 Redis DLMStatus 读取
     */
    private Map<String, Object> handleGetWorkMode(String method, String deviceSn) {
        JSONObject dlm = getDlmData(deviceSn);
        List<Map<String, Object>> deviceList = new ArrayList<>();

        if (dlm != null && dlm.containsKey("pileAllocations")) {
            for (Object obj : dlm.getJSONArray("pileAllocations")) {
                JSONObject pile = (JSONObject) obj;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("mac", pile.getString("sn"));
                item.put("subDevId", pile.getString("sn"));
                item.put("workMode", pile.containsKey("workMode") ? pile.getString("workMode") : "unknown");
                deviceList.add(item);
            }
        }

        return rpcSuccess(method, deviceSn, Map.of("deviceList", deviceList));
    }

    /**
     * 设置充电桩工作模式 — OCPP DataTransfer(SetWorkMode)
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> handleSetWorkMode(String method, String deviceSn, Map<String, Object> data) {
        List<Map<String, String>> rawList = (List<Map<String, String>>) data.get("deviceList");
        if (rawList == null || rawList.isEmpty()) {
            return rpcError(method, 400, "deviceList 不能为空");
        }

        // App 端用 mac 字段标识桩，映射为 OCPP 协议的 sn 字段
        List<Map<String, String>> deviceList = new ArrayList<>();
        for (Map<String, String> raw : rawList) {
            String pileSn = raw.get("mac") != null ? raw.get("mac") : raw.get("sn");
            Map<String, String> item = new LinkedHashMap<>();
            item.put("sn", pileSn);
            item.put("workMode", raw.get("workMode"));
            deviceList.add(item);
        }

        try {
            deviceService.sendWorkMode(deviceSn, deviceList, "app");
        } catch (Exception e) {
            return rpcError(method, 400, e.getMessage());
        }

        return rpcSuccess(method, deviceSn, Map.of());
    }

    // ═══════════════════════════════════════════════
    // 远程固件升级
    // ═══════════════════════════════════════════════

    /**
     * 请求固件更新 — OCPP UpdateFirmware
     */
    private Map<String, Object> handleRequestFirmwareUpdate(String method, String deviceSn) {
        // 查最新已发布固件
        FirmwareLatest latest = firmwareVersionService.getLatest(BizConstant.TYPE_N3_LITE);
        if (latest == null || latest.getLatestFirmwareId() == null) {
            return rpcError(method, 400, "暂无已发布的固件版本");
        }

        // 检查设备当前版本
        NcDevice device = deviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, deviceSn));
        if (device == null) {
            return rpcError(method, 400, "设备不存在");
        }
        String currentVersion = device.getFirmwareVersion();
        if (currentVersion != null && FirmwareVersionServiceImpl.compareVersion(
                currentVersion, latest.getLatestVersion()) >= 0) {
            return rpcError(method, 400, "当前已是最新版本，无需更新");
        }

        // 检查设备在线
        if (!ocppCommandSender.isDeviceConnected(deviceSn)) {
            return rpcError(method, 400, "设备不在线，无法下发更新");
        }

        // 获取固件文件 URL
        FirmwareVersion fw = firmwareVersionService.getById(latest.getLatestFirmwareId());
        if (fw == null || fw.getFileUrl() == null) {
            return rpcError(method, 400, "固件文件不存在");
        }

        String downloadUrl;
        try {
            String bucketName = MinioUtil.getBucketName();
            String objectName = MinioUtil.extractObjectName(fw.getFileUrl(), bucketName);
            downloadUrl = MinioUtil.getObjectUrl(bucketName, objectName, 3600);
        } catch (Exception e) {
            log.error("[AppOTA] 生成下载链接失败", e);
            return rpcError(method, 500, "生成固件下载链接失败");
        }

        // 创建升级任务
        FirmwareUpgradeTask task = new FirmwareUpgradeTask();
        task.setFirmwareId(fw.getId());
        task.setDeviceSn(deviceSn);
        task.setStatus(BizConstant.TASK_PENDING);
        task.setProgress(0);
        task.setStartTime(new Date());
        upgradeTaskService.save(task);
        String taskId = task.getId();

        // OCPP UpdateFirmware 下发
        String messageId = "ota-" + UUID.randomUUID().toString().substring(0, 8);
        JsonObject payload = new JsonObject();
        payload.addProperty("location", downloadUrl);
        payload.addProperty("retrieveDate", java.time.Instant.now().toString());
        payload.addProperty("retries", 1);
        payload.addProperty("retryInterval", 30);

        JsonArray call = new JsonArray();
        call.add(2);
        call.add(messageId);
        call.add("UpdateFirmware");
        call.add(payload);

        log.info("[AppOTA] RPC触发云端OTA — 设备={}, 目标版本={}, taskId={}", deviceSn, fw.getVersion(), taskId);
        ocppCommandSender.sendCall(deviceSn, call.toString());

        // 推送初始状态
        JSONObject wsMsg = new JSONObject();
        wsMsg.put("taskId", taskId);
        wsMsg.put("deviceSn", deviceSn);
        wsMsg.put("status", BizConstant.TASK_PENDING);
        wsMsg.put("progress", 0);
        wsMsg.put("message", "升级指令已下发，等待设备响应");
        wsMsg.put("message_en", "Upgrade command sent, waiting for device response");
        wsMsg.put("message_tw", "升級指令已下發，等待裝置回應");
        AppWebSocket.publish("ota:" + taskId, wsMsg);

        return rpcSuccess(method, deviceSn, Map.of(
                "message", "固件更新指令已下发",
                "targetVersion", fw.getVersion(),
                "taskId", taskId));
    }

    // ═══════════════════════════════════════════════
    // 工具方法
    // ═══════════════════════════════════════════════

    private JSONObject getDlmData(String deviceSn) {
        Object raw = redisTemplate.opsForValue().get("device:dlm:" + deviceSn);
        if (raw == null) {
            return null;
        }
        try {
            return JSONObject.parseObject(raw.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private JSONObject findPile(JSONObject dlm, String mac) {
        if (dlm == null || !dlm.containsKey("pileAllocations")) {
            return null;
        }
        for (Object obj : dlm.getJSONArray("pileAllocations")) {
            JSONObject pile = (JSONObject) obj;
            if (mac.equals(pile.getString("sn"))) {
                return pile;
            }
        }
        return null;
    }

    private String getMac(Map<String, Object> data) {
        if (data.containsKey("deviceInfo")) {
            Map<String, Object> deviceInfo = (Map<String, Object>) data.get("deviceInfo");
            return (String) deviceInfo.get("mac");
        }
        return null;
    }

    private String getDeviceId(String deviceSn) {
        NcDevice device = deviceService.getOne(
                new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, deviceSn));
        return device != null ? device.getId() : null;
    }

    private Map<String, Object> rpcSuccess(String method, String deviceSn, Map<String, Object> data) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ret", Map.of(method, "success"));
        result.put("deviceId", deviceSn);
        result.put("code", 200);
        result.put("data", data);
        return result;
    }

    private Map<String, Object> rpcError(String method, int code, String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ret", Map.of(method != null ? method : "unknown", "failed"));
        result.put("code", code);
        result.put("data", Map.of());
        result.put("message", com.echarge.modules.app.i18n.AppI18n.get(message,
                com.echarge.modules.app.i18n.LangContext.get()));
        return result;
    }
}
