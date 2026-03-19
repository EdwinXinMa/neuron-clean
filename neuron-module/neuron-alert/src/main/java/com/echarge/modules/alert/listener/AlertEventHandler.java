package com.echarge.modules.alert.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.common.event.DeviceEvent;
import com.echarge.common.event.DeviceEventListener;
import com.echarge.modules.alert.service.INcAlertService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 告警事件监听 — 接收 protocol 模块发来的设备事件，处理告警逻辑
 * 与 DeviceEventHandler 并行工作，各自处理各自的业务
 */
@Slf4j
@Component
public class AlertEventHandler implements DeviceEventListener {

    @Autowired
    private INcAlertService ncAlertService;

    private final Gson gson = new Gson();

    @Override
    public void onDeviceEvent(DeviceEvent event) {
        if (DeviceEvent.STATUS_NOTIFICATION.equals(event.getEventType())) {
            handleStatusNotification(event);
        }
    }

    /**
     * 处理 StatusNotification — 故障触发告警，恢复自动关闭告警
     */
    private void handleStatusNotification(DeviceEvent event) {
        String chargePointId = event.getChargePointId();
        JsonObject payload = gson.fromJson(event.getPayload(), JsonObject.class);

        String status = getJsonString(payload, "status");
        String errorCode = getJsonString(payload, "errorCode");
        String vendorErrorCode = getJsonString(payload, "vendorErrorCode");
        String info = getJsonString(payload, "info");
        Integer connectorId = payload.has("connectorId") ? payload.get("connectorId").getAsInt() : null;

        if ("Faulted".equals(status) && errorCode != null && !"NoError".equals(errorCode)) {
            // 故障 → 触发告警
            ncAlertService.triggerAlert(chargePointId, connectorId, errorCode, vendorErrorCode, info);
        } else if (!"Faulted".equals(status)) {
            // 恢复正常 → 自动关闭该设备/枪的告警
            ncAlertService.resolveAlertAuto(chargePointId, connectorId);
        }
    }

    private String getJsonString(JsonObject obj, String key) {
        return obj != null && obj.has(key) && !obj.get(key).isJsonNull()
                ? obj.get(key).getAsString() : null;
    }
}
