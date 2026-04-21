package com.echarge.modules.app.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echarge.modules.app.entity.AppUser;
import com.echarge.modules.app.entity.AppUserDevice;
import com.echarge.modules.app.mapper.AppUserDeviceMapper;
import com.echarge.common.ocpp.OcppCommandSender;
import com.echarge.modules.device.entity.NcChargingSession;
import com.echarge.modules.device.entity.NcConnector;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.mapper.NcChargingSessionMapper;
import com.echarge.modules.device.service.INcConnectorService;
import com.echarge.modules.device.service.INcDeviceService;
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
            // 云模式不需要的接口
            case "SubDeviceManager.SearchChargeStationRequest",
                 "SubDeviceManager.UpdateMajorSubDeviceByPortName",
                 "SubDeviceManager.SetChargingStationWorkMode" ->
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
            result.put("energy", String.valueOf(session.getEnergy() != null ? session.getEnergy() : 0));
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
                result.put("EVStatus", "Finishing");
                result.put("energy", String.valueOf(finished.getEnergy() != null ? finished.getEnergy() : 0));
                result.put("duration", String.valueOf(finished.getDuration() != null ? finished.getDuration() : 0));
                result.put("chargingMethod", finished.getChargingMethod() != null ? finished.getChargingMethod() : 0);
                result.put("isPlugged", 0);
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
        deviceInfo.put("energy", pile != null ? String.valueOf(pile.getIntValue("energy")) : "0");

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
                item.put("ChargingCurrent", pile.getString("allocatedCurrent"));
                item.put("energy", String.valueOf(pile.getIntValue("energy")));
                item.put("EVStatus", pile.getString("charge_EVStatus"));
                item.put("connectStatus", pile.getString("connectStatus"));
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

        Page<NcChargingSession> result = chargingSessionMapper.selectPage(
                new Page<>(page + 1, pageSize),
                new LambdaQueryWrapper<NcChargingSession>()
                        .eq(NcChargingSession::getDeviceSn, deviceSn)
                        .orderByDesc(NcChargingSession::getStartTime));

        List<Map<String, Object>> historyList = new ArrayList<>();
        for (NcChargingSession s : result.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId());
            item.put("startTime", s.getStartTime() != null ? String.valueOf(s.getStartTime().getTime() / 1000) : "");
            item.put("endTime", s.getEndTime() != null ? String.valueOf(s.getEndTime().getTime() / 1000) : "");
            item.put("energy", String.valueOf(s.getEnergy() != null ? s.getEnergy() : 0));
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
            return rpcError(method, 504, "设备响应超时");
        }

        // 解析响应
        JsonObject respObj = JsonParser.parseString(response).getAsJsonObject();
        String status = respObj.has("status") ? respObj.get("status").getAsString() : "Rejected";
        if (!"Accepted".equals(status)) {
            return rpcError(method, 400, "设备拒绝启动充电（" + status + "）");
        }

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
            return rpcError(method, 504, "设备响应超时");
        }

        // 解析响应
        JsonObject respObj = JsonParser.parseString(response).getAsJsonObject();
        String status = respObj.has("status") ? respObj.get("status").getAsString() : "Rejected";
        if (!"Accepted".equals(status)) {
            return rpcError(method, 400, "设备拒绝停止充电（" + status + "）");
        }

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
                break;
            }
        }

        return rpcSuccess(method, deviceSn, Map.of());
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
        result.put("message", message);
        return result;
    }
}
