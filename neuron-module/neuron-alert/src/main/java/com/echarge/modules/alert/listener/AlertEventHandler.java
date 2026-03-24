package com.echarge.modules.alert.listener;

import com.echarge.common.event.DeviceEvent;
import com.echarge.common.event.DeviceEventListener;
import com.echarge.modules.alert.service.INcAlertService;
import com.echarge.common.websocket.FrontendPushChannel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 告警事件监听（v3.0 精简版：只插入告警记录，不做状态流转）
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
     * StatusNotification 故障时插入告警记录
     */
    private void handleStatusNotification(DeviceEvent event) {
        String chargePointId = event.getChargePointId();
        JsonObject payload = gson.fromJson(event.getPayload(), JsonObject.class);

        String status = getStr(payload, "status");
        String errorCode = getStr(payload, "errorCode");
        String vendorErrorCode = getStr(payload, "vendorErrorCode");
        String info = getStr(payload, "info");
        Integer connectorId = payload.has("connectorId") ? payload.get("connectorId").getAsInt() : null;

        if ("Faulted".equals(status) && errorCode != null && !"NoError".equals(errorCode)) {
            ncAlertService.recordAlert(chargePointId, connectorId, errorCode, vendorErrorCode, info);

            // 广播告警事件到前端
            JsonObject msg = new JsonObject();
            msg.addProperty("type", "ALERT");
            msg.addProperty("deviceSn", chargePointId);
            msg.addProperty("errorCode", errorCode);
            msg.addProperty("detail", info != null ? info : errorCode);
            msg.addProperty("timestamp", java.time.Instant.now().toString());
            FrontendPushChannel.broadcast(msg.toString());
        }
    }

    private String getStr(JsonObject obj, String key) {
        return obj != null && obj.has(key) && !obj.get(key).isJsonNull()
                ? obj.get(key).getAsString() : null;
    }
}
