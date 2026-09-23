package com.echarge.modules.device.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.common.constant.BizConstant;
import com.echarge.common.exception.NeuronBootException;
import com.echarge.common.ocpp.OcppCommandSender;
import com.echarge.common.util.RedisUtil;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.entity.NcOpLog;
import com.echarge.modules.device.mapper.NcDeviceMapper;
import com.echarge.modules.device.service.INcDeviceService;
import com.echarge.modules.device.service.INcOpLogService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Edwin
 */
@Slf4j
@Service
public class NcDeviceServiceImpl extends ServiceImpl<NcDeviceMapper, NcDevice> implements INcDeviceService {

    private static final long ALLOCATION_RESPONSE_TIMEOUT_SECONDS = 10L;

    @Autowired
    private OcppCommandSender ocppCommandSender;

    @Autowired
    private RedisUtil redisClient;

    @Autowired
    private INcOpLogService opLogService;

    /** {@inheritDoc} */
    @Override
    public void register(NcDevice device) {
        String sn = device.getSn();
        if (StringUtils.isNotBlank(sn)) {
            sn = sn.trim().replaceAll("\\s+", "");
            device.setSn(sn);
        }
        if (StringUtils.isBlank(device.getSn())) {
            throw new NeuronBootException("\u8bbe\u5907SN\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (StringUtils.isBlank(device.getDealer())) {
            throw new NeuronBootException("\u7ecf\u9500\u5546\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (device.getShipDate() == null) {
            throw new NeuronBootException("\u51fa\u8d27\u65e5\u671f\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (existsBySn(device.getSn())) {
            throw new NeuronBootException("\u8bbe\u5907SN\u5df2\u5b58\u5728");
        }
        device.setDeviceType(BizConstant.TYPE_N3_LITE);
        device.setOnlineStatus(BizConstant.DEVICE_UNACTIVATED);
        device.setStatus("NORMAL");
        device.setDelFlag(0);
        assignRandomLocation(device);
        this.save(device);
    }

    /** {@inheritDoc} */
    @Override
    public void disable(String id) {
        NcDevice device = this.getById(id);
        if (device == null) {
            throw new NeuronBootException("\u8bbe\u5907\u4e0d\u5b58\u5728");
        }
        device.setStatus("DISABLED");
        this.updateById(device);
    }

    /** {@inheritDoc} */
    @Override
    public void enable(String id) {
        NcDevice device = this.getById(id);
        if (device == null) {
            throw new NeuronBootException("\u8bbe\u5907\u4e0d\u5b58\u5728");
        }
        device.setStatus("NORMAL");
        this.updateById(device);
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsBySn(String sn) {
        return this.count(new LambdaQueryWrapper<NcDevice>()
                .eq(NcDevice::getSn, sn)
                .eq(NcDevice::getDelFlag, 0)) > 0;
    }

    /** {@inheritDoc} */
    @Override
    public void sendDlmConfig(String sn, int breakerRating, Integer safetyMargin, String opUser) {
        NcDevice device = this.getOne(new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn));
        if (device == null) {
            throw new NeuronBootException("设备不存在: " + sn);
        }
        if (safetyMargin != null && safetyMargin < 0) {
            throw new NeuronBootException("safetyMargin 不能为负数");
        }
        if (safetyMargin == null) {
            if (!BizConstant.VALID_BREAKER_RATINGS.contains(breakerRating)) {
                throw new NeuronBootException("breakerRating 无效");
            }
        } else {
            boolean isMinRange = breakerRating >= BizConstant.DLM_MIN_BREAKER_RATING_MIN
                    && breakerRating <= BizConstant.DLM_MIN_BREAKER_RATING_MAX;
            if (isMinRange) {
                if (safetyMargin != 0) {
                    throw new NeuronBootException("safetyMargin 超出允许范围");
                }
            } else {
                if (!BizConstant.VALID_BREAKER_RATINGS.contains(breakerRating)) {
                    throw new NeuronBootException("breakerRating 无效");
                }
                Integer maxMargin = BizConstant.MAX_SAFETY_MARGIN.get(breakerRating);
                if (maxMargin != null && safetyMargin > maxMargin) {
                    throw new NeuronBootException("safetyMargin 超出允许范围");
                }
            }
        }

        Integer oldRating = device.getBreakerRating();
        Integer oldMargin = device.getSafetyMargin();

        // 更新数据库
        device.setBreakerRating(breakerRating);
        if (safetyMargin != null) {
            device.setSafetyMargin(safetyMargin);
        }
        this.updateById(device);

        // 同步更新 Redis
        String redisKey = "device:dlm:" + sn;
        Object dlmRaw = redisClient.get(redisKey);
        if (dlmRaw != null) {
            try {
                JSONObject dlm = JSONObject.parseObject(dlmRaw.toString());
                dlm.put("breakerRating", breakerRating);
                if (safetyMargin != null) {
                    dlm.put("safetyMargin", safetyMargin);
                }
                redisClient.set(redisKey, dlm.toJSONString(), 300);
            } catch (Exception e) {
                log.warn("更新 Redis DLM 数据失败: {}", e.getMessage());
            }
        }

        // OCPP 下发
        if (ocppCommandSender.isDeviceConnected(sn)) {
            String messageId = "dlm-" + java.util.UUID.randomUUID().toString().substring(0, 8);
            JsonObject payload = new JsonObject();
            payload.addProperty("breakerRating", breakerRating);
            if (safetyMargin != null) {
                payload.addProperty("safetyMargin", safetyMargin);
            }

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
            log.info("[DLM] Config sent to {}: breakerRating={}A, safetyMargin={}", sn, breakerRating,
                    safetyMargin != null ? safetyMargin + "A" : "not sent");
        }

        // 操作日志
        NcOpLog opLog = new NcOpLog();
        opLog.setDeviceSn(sn);
        opLog.setOpUser(opUser);
        opLog.setOpType(NcOpLog.DLM_CONFIG);
        opLog.setOpContent((oldRating != null ? oldRating : "?") + "A/"
                + (oldMargin != null ? oldMargin : "-") + "A → " + breakerRating + "A/"
                + (safetyMargin != null ? safetyMargin : "-") + "A");
        opLog.setOpResult(NcOpLog.SUCCESS);
        opLog.setOpTime(new Date());
        opLog.setCreateTime(new Date());
        opLogService.save(opLog);
    }

    /** {@inheritDoc} */
    @Override
    public void sendWorkMode(String sn, List<Map<String, String>> deviceList, String opUser) {
        NcDevice device = this.getOne(new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn));
        if (device == null) {
            throw new NeuronBootException("设备不存在: " + sn);
        }
        if (!ocppCommandSender.isDeviceConnected(sn)) {
            throw new NeuronBootException("设备离线，无法下发工作模式切换");
        }

        Set<String> validModes = Set.of("Manual", "Auto", "Scheduled");
        for (Map<String, String> item : deviceList) {
            if (item.get("sn") == null || item.get("sn").isBlank()) {
                throw new NeuronBootException("桩 SN 不能为空");
            }
            if (!validModes.contains(item.get("workMode"))) {
                throw new NeuronBootException("workMode 必须是 Manual/Auto/Scheduled 之一");
            }
        }

        // 构建 OCPP DataTransfer CALL
        JsonObject payload = new JsonObject();
        com.google.gson.JsonArray list = new com.google.gson.JsonArray();
        StringBuilder desc = new StringBuilder();
        for (Map<String, String> item : deviceList) {
            JsonObject obj = new JsonObject();
            obj.addProperty("sn", item.get("sn"));
            obj.addProperty("workMode", item.get("workMode"));
            list.add(obj);
            if (!desc.isEmpty()) {
                desc.append(", ");
            }
            desc.append(item.get("sn")).append("→").append(item.get("workMode"));
        }
        payload.add("deviceList", list);

        String messageId = "wm-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        JsonArray call = new JsonArray();
        call.add(2);
        call.add(messageId);
        call.add("DataTransfer");

        JsonObject dtPayload = new JsonObject();
        dtPayload.addProperty("vendorId", "AlwaysControl");
        dtPayload.addProperty("messageId", BizConstant.DT_SET_WORK_MODE);
        dtPayload.addProperty("data", payload.toString());
        call.add(dtPayload);

        ocppCommandSender.sendCall(sn, call.toString());
        log.info("[WorkMode] Command sent to {}: {}", sn, desc);

        // 操作日志
        NcOpLog opLog = new NcOpLog();
        opLog.setDeviceSn(sn);
        opLog.setOpUser(opUser);
        opLog.setOpType(NcOpLog.WORK_MODE);
        opLog.setOpContent("工作模式切换: " + desc);
        opLog.setOpResult(NcOpLog.SUCCESS);
        opLog.setOpTime(new Date());
        opLog.setCreateTime(new Date());
        opLogService.save(opLog);
    }

    /** {@inheritDoc} */
    @Override
    public void sendScheduledCharging(String sn, String pileSn, List<List<String>> timePeriods, String opUser) {
        validateScheduleRequest(sn, pileSn);
        if (timePeriods == null) {
            throw new NeuronBootException("timePeriods 不能为 null");
        }
        if (timePeriods.size() > 11) {
            throw new NeuronBootException("timePeriods 不能超过 11 段");
        }

        String messageId = "sch-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        JsonObject devicePayload = new JsonObject();
        devicePayload.addProperty("sn", pileSn);
        devicePayload.add("timePeriods", toTimePeriodsJson(timePeriods));

        JsonArray deviceList = new JsonArray();
        deviceList.add(devicePayload);

        JsonObject payload = new JsonObject();
        payload.add("deviceList", deviceList);

        JsonArray call = buildDataTransferCall(messageId, BizConstant.DT_SET_SCHEDULED_CHARGING, payload.toString());
        log.info("[ScheduledCharging] Command sending to {}: pileSn={}, timePeriods={}", sn, pileSn, timePeriods);
        JsonObject respObj = parseAcceptedResponse(
                ocppCommandSender.sendCallAndWait(sn, call.toString(), messageId, 10),
                "设备响应超时",
                "设备拒绝设置预约充电时间段");

        saveScheduledChargingLog(sn, opUser, pileSn, timePeriods, NcOpLog.SUCCESS, null);
        log.info("[ScheduledCharging] Command accepted by {}: pileSn={}, response={}", sn, pileSn, respObj);
    }

    /** {@inheritDoc} */
    @Override
    public List<List<String>> getScheduledCharging(String sn, String pileSn) {
        validateScheduleRequest(sn, pileSn);

        String messageId = "gsch-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        JsonObject payload = new JsonObject();
        payload.addProperty("sn", pileSn);

        JsonArray call = buildDataTransferCall(messageId, BizConstant.DT_GET_SCHEDULED_CHARGING, payload.toString());
        log.info("[ScheduledCharging] Query sending to {}: pileSn={}", sn, pileSn);
        JsonObject respObj = parseAcceptedResponse(
                ocppCommandSender.sendCallAndWait(sn, call.toString(), messageId, 10),
                "设备响应超时",
                "设备拒绝查询预约充电时间段");
        if (!respObj.has("timePeriods") || !respObj.get("timePeriods").isJsonArray()) {
            throw new NeuronBootException("设备返回的 timePeriods 格式无效");
        }

        List<List<String>> result = new ArrayList<>();
        for (JsonElement element : respObj.getAsJsonArray("timePeriods")) {
            if (!element.isJsonArray() || element.getAsJsonArray().size() != 2) {
                throw new NeuronBootException("设备返回的 timePeriods 格式无效");
            }
            JsonArray pair = element.getAsJsonArray();
            result.add(List.of(pair.get(0).getAsString(), pair.get(1).getAsString()));
        }
        return result;
    }

    private void validateScheduleRequest(String sn, String pileSn) {
        NcDevice device = this.getOne(new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn));
        if (device == null) {
            throw new NeuronBootException("设备不存在: " + sn);
        }
        if (StringUtils.isBlank(pileSn)) {
            throw new NeuronBootException("桩 SN 不能为空");
        }
        if (!ocppCommandSender.isDeviceConnected(sn)) {
            throw new NeuronBootException("设备离线，无法下发预约充电指令");
        }
    }

    private JsonArray buildDataTransferCall(String messageId, String dataTransferMessageId, String data) {
        JsonArray call = new JsonArray();
        call.add(2);
        call.add(messageId);
        call.add("DataTransfer");

        JsonObject dtPayload = new JsonObject();
        dtPayload.addProperty("vendorId", "AlwaysControl");
        dtPayload.addProperty("messageId", dataTransferMessageId);
        dtPayload.addProperty("data", data);
        call.add(dtPayload);
        return call;
    }

    private JsonArray toTimePeriodsJson(List<List<String>> timePeriods) {
        JsonArray array = new JsonArray();
        for (List<String> period : timePeriods) {
            if (period == null || period.size() != 2) {
                throw new NeuronBootException("timePeriods 格式必须是 [[\"HH:mm\",\"HH:mm\"]]");
            }
            JsonArray pair = new JsonArray();
            pair.add(period.get(0));
            pair.add(period.get(1));
            array.add(pair);
        }
        return array;
    }

    /** {@inheritDoc} */
    @Override
    public void setAllocationMode(String sn, String allocationMode, String opUser) {
        log.info("[AllocationMode] Setting requested: sn={}, mode={}", sn, allocationMode);
        if (allocationMode == null || !BizConstant.VALID_ALLOCATION_MODES.contains(allocationMode)) {
            throw new NeuronBootException("AllocationMode 必须为 Average 或 FIFO", 400);
        }
        String result = NcOpLog.FAIL;
        String failReason = null;
        try {
            validateAllocationDevice(sn);
            JsonObject payload = new JsonObject();
            payload.addProperty(BizConstant.ALLOCATION_MODE, allocationMode);
            sendAllocationCommand(sn, BizConstant.DT_SET_ALLOCATION_MODE, payload.toString());
            result = NcOpLog.SUCCESS;
            log.info("[AllocationMode] Setting confirmed: sn={}, mode={}", sn, allocationMode);
        } catch (RuntimeException e) {
            failReason = e instanceof NeuronBootException ? e.getMessage() : "电流分配模式操作失败";
            log.info("[AllocationMode] Setting failed: sn={}, mode={}, reason={}", sn, allocationMode, failReason);
            throw e;
        } finally {
            saveAllocationModeLog(sn, allocationMode, opUser, result, failReason);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getAllocationMode(String sn) {
        log.info("[AllocationMode] Query requested: sn={}", sn);
        validateAllocationDevice(sn);
        JsonObject response = sendAllocationCommand(sn, BizConstant.DT_GET_ALLOCATION_MODE, "");
        JsonElement mode = response.get(BizConstant.ALLOCATION_MODE);
        if (!isJsonString(mode) || !BizConstant.VALID_ALLOCATION_MODES.contains(mode.getAsString())) {
            log.info("[AllocationMode] Query failed: sn={}, invalid AllocationMode", sn);
            throw new NeuronBootException("设备返回的电流分配模式格式无效");
        }
        log.info("[AllocationMode] Query confirmed: sn={}, mode={}", sn, mode.getAsString());
        return mode.getAsString();
    }

    private void validateAllocationDevice(String sn) {
        if (StringUtils.isBlank(sn)) {
            throw new NeuronBootException("deviceSn 必须为非空字符串", 400);
        }
        NcDevice device = getOne(new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn));
        if (device == null) {
            throw new NeuronBootException("设备不存在", 404);
        }
        if (!BizConstant.TYPE_N3_LITE.equals(device.getDeviceType())
                || StringUtils.isNotBlank(device.getParentDeviceId())) {
            throw new NeuronBootException("电流分配模式仅支持 N3lite 网关", 400);
        }
        if (!ocppCommandSender.isDeviceConnected(sn)) {
            throw new NeuronBootException("设备离线，无法操作电流分配模式");
        }
    }

    private JsonObject sendAllocationCommand(String sn, String command, String data) {
        String messageId = "am-" + java.util.UUID.randomUUID().toString().replace("-", "");
        JsonArray call = buildDataTransferCall(messageId, command, data);
        log.info("[AllocationMode] Sending: sn={}, command={}, messageId={}, data={}", sn, command, messageId, data);
        String response = ocppCommandSender.sendCallAndWait(sn, call.toString(), messageId,
                ALLOCATION_RESPONSE_TIMEOUT_SECONDS);
        if (response == null) {
            log.info("[AllocationMode] No confirmation: sn={}, command={}, messageId={}", sn, command, messageId);
            throw new NeuronBootException("设备未返回有效响应");
        }
        JsonObject payload;
        try {
            payload = JsonParser.parseString(response).getAsJsonObject();
        } catch (RuntimeException e) {
            throw new NeuronBootException("设备返回的电流分配模式格式无效");
        }
        JsonElement status = payload.get("status");
        if (!isJsonString(status)) {
            throw new NeuronBootException("设备返回的电流分配模式格式无效");
        }
        log.info("[AllocationMode] Response: sn={}, command={}, messageId={}, status={}",
                sn, command, messageId, status.getAsString());
        if (!"Accepted".equals(status.getAsString())) {
            throw new NeuronBootException(BizConstant.DT_SET_ALLOCATION_MODE.equals(command)
                    ? "设备拒绝设置电流分配模式" : "设备拒绝查询电流分配模式");
        }
        return payload;
    }

    private boolean isJsonString(JsonElement value) {
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString();
    }

    private void saveAllocationModeLog(String sn, String mode, String opUser, String result, String failReason) {
        NcOpLog opLog = new NcOpLog();
        opLog.setDeviceSn(sn);
        opLog.setOpUser(opUser);
        opLog.setOpType(NcOpLog.DLM_CONFIG);
        opLog.setOpContent(BizConstant.ALLOCATION_MODE + " -> " + mode);
        opLog.setOpResult(result);
        opLog.setFailReason(failReason);
        opLog.setOpTime(new Date());
        opLog.setCreateTime(new Date());
        // 日志写入失败不改变固件已确认的配置结果，也不覆盖原始下发异常。
        try {
            if (!opLogService.save(opLog)) {
                log.error("[AllocationMode] Operation log not saved: sn={}, result={}", sn, result);
            }
        } catch (RuntimeException e) {
            log.error("[AllocationMode] Operation log failed: sn={}, result={}", sn, result, e);
        }
    }

    private JsonObject parseAcceptedResponse(String response, String timeoutMessage, String rejectMessage) {
        if (response == null) {
            throw new NeuronBootException(timeoutMessage);
        }
        JsonObject respObj = JsonParser.parseString(response).getAsJsonObject();
        String status = respObj.has("status") ? respObj.get("status").getAsString() : "Rejected";
        if (!"Accepted".equals(status)) {
            String reason = respObj.has("message") ? respObj.get("message").getAsString() : status;
            throw new NeuronBootException(rejectMessage + "（" + reason + "）");
        }
        return respObj;
    }

    private void saveScheduledChargingLog(String sn, String opUser, String pileSn, List<List<String>> timePeriods,
                                          String result, String failReason) {
        NcOpLog opLog = new NcOpLog();
        opLog.setDeviceSn(sn);
        opLog.setOpUser(opUser);
        opLog.setOpType(NcOpLog.SCHEDULED_CHARGING);
        opLog.setOpContent("预约充电时间段: " + pileSn + " -> " + timePeriods);
        opLog.setOpResult(result);
        opLog.setFailReason(failReason);
        opLog.setOpTime(new Date());
        opLog.setCreateTime(new Date());
        opLogService.save(opLog);
    }

    /** {@inheritDoc} */
    @Override
    public void sendFactoryReset(String sn, String requestedBy, String opUser) {
        NcDevice device = this.getOne(new LambdaQueryWrapper<NcDevice>().eq(NcDevice::getSn, sn));
        if (device == null) {
            throw new NeuronBootException("设备不存在: " + sn);
        }
        if (!ocppCommandSender.isDeviceConnected(sn)) {
            throw new NeuronBootException("设备离线，无法恢复出厂设置");
        }

        String messageId = "fr-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        // 设备不强制要求包含以下字段，可以为空，当前下发 data 字段为空字符串。
        // JsonObject payload = new JsonObject();
        // payload.addProperty("confirm", true);
        // payload.addProperty("scope", "AllUserConfig");
        // payload.addProperty("reboot", true);
        // payload.addProperty("requestedBy", requestedBy);

        JsonArray call = new JsonArray();
        call.add(2);
        call.add(messageId);
        call.add("DataTransfer");

        JsonObject dtPayload = new JsonObject();
        dtPayload.addProperty("vendorId", "AlwaysControl");
        dtPayload.addProperty("messageId", BizConstant.DT_FACTORY_RESET);
        dtPayload.addProperty("data", "");
        call.add(dtPayload);

        log.info("[FactoryReset] Command sending to {}: requestedBy={}, messageId={}", sn, requestedBy, messageId);
        String response = ocppCommandSender.sendCallAndWait(sn, call.toString(), messageId, 10);
        if (response == null) {
            saveFactoryResetLog(sn, opUser, NcOpLog.FAIL, "设备响应超时");
            throw new NeuronBootException("设备响应超时");
        }

        JsonObject respObj = JsonParser.parseString(response).getAsJsonObject();
        String status = respObj.has("status") ? respObj.get("status").getAsString() : "Rejected";
        if (!"Accepted".equals(status)) {
            String reason = respObj.has("message") ? respObj.get("message").getAsString() : status;
            saveFactoryResetLog(sn, opUser, NcOpLog.FAIL, reason);
            throw new NeuronBootException("设备拒绝恢复出厂设置（" + reason + "）");
        }

        saveFactoryResetLog(sn, opUser, NcOpLog.SUCCESS, null);
        log.info("[FactoryReset] Command accepted by {}: requestedBy={}, messageId={}", sn, requestedBy, messageId);
        // 恢复出厂会直接清除网络配置，设备可能不会发送 TCP close；主动关闭旧会话，避免半开连接保持在线 180 秒。
        boolean connectionClosed = ocppCommandSender.closeDeviceConnection(sn);
        if (connectionClosed) {
            log.info("[FactoryReset] OCPP connection closed after device acceptance: sn={}", sn);
        } else {
            log.info("[FactoryReset] OCPP connection was already inactive after device acceptance: sn={}", sn);
        }
    }

    private void saveFactoryResetLog(String sn, String opUser, String result, String failReason) {
        NcOpLog opLog = new NcOpLog();
        opLog.setDeviceSn(sn);
        opLog.setOpUser(opUser);
        opLog.setOpType(NcOpLog.REMOTE_RESET);
        opLog.setOpContent("恢复出厂设置");
        opLog.setOpResult(result);
        opLog.setFailReason(failReason);
        opLog.setOpTime(new Date());
        opLog.setCreateTime(new Date());
        opLogService.save(opLog);
    }

    /**
     * 如果设备没有经纬度，随机分配一个全球陆地坐标
     */
    public static void assignRandomLocation(NcDevice device) {
        if (device.getLat() != null && device.getLng() != null) {
            return;
        }
        // 全球主要城市坐标 { 纬度, 经度 } + 小范围偏移，保证落在陆地上
        double[][] cities = {
            // 北京
            { 39.9, 116.4 },
            // 上海
            { 31.2, 121.5 },
            // 广州
            { 23.1, 113.3 },
            // 深圳
            { 22.5, 114.1 },
            // 东京
            { 35.7, 139.7 },
            // 首尔
            { 37.6, 127.0 },
            // 新加坡
            { 1.35, 103.8 },
            // 曼谷
            { 13.8, 100.5 },
            // 新德里
            { 28.6, 77.2 },
            // 伦敦
            { 51.5, -0.1 },
            // 巴黎
            { 48.9, 2.35 },
            // 柏林
            { 52.5, 13.4 },
            // 马德里
            { 40.4, -3.7 },
            // 罗马
            { 41.9, 12.5 },
            // 纽约
            { 40.7, -74.0 },
            // 洛杉矶
            { 34.1, -118.2 },
            // 多伦多
            { 43.7, -79.4 },
            // 圣保罗
            { -23.6, -46.6 },
            // 开普敦
            { -33.9, 18.4 },
            // 悉尼
            { -33.9, 151.2 },
            // 墨尔本
            { -37.8, 145.0 },
            // 莫斯科
            { 55.8, 37.6 },
            // 迪拜
            { 25.3, 55.3 },
        };
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double[] city = cities[rng.nextInt(cities.length)];
        // ±1度 ≈ ±111km
        double lat = city[0] + (rng.nextDouble() - 0.5) * 2;
        double lng = city[1] + (rng.nextDouble() - 0.5) * 2;
        device.setLat(BigDecimal.valueOf(lat).setScale(6, RoundingMode.HALF_UP));
        device.setLng(BigDecimal.valueOf(lng).setScale(6, RoundingMode.HALF_UP));
    }
}
