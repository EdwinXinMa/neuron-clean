package com.echarge.modules.alert.listener;

import com.echarge.common.event.DeviceEvent;
import com.echarge.common.event.DeviceEventListener;
import com.echarge.common.event.kafka.KafkaTopics;
import com.echarge.modules.alert.service.INcAlertService;
import com.echarge.common.websocket.FrontendPushChannel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 告警事件监听
 * 直连模式：从 LocalDeviceEventPublisher 接收所有事件，过滤 STATUS_NOTIFICATION
 * Kafka 模式：从 device-alert topic 消费（已经是确认的告警，无需再过滤）
 */
@Slf4j
@Component
public class AlertEventHandler implements DeviceEventListener {

    @Autowired
    private INcAlertService ncAlertService;

    private final Gson gson = new Gson();

    // ==================== Kafka 消费入口 ====================

    @KafkaListener(
            topics = KafkaTopics.DEVICE_ALERT,
            groupId = "alert-handler",
            autoStartup = "${neuron.kafka.enabled:false}"
    )
    public void onAlertMessage(ConsumerRecord<String, String> record) {
        DeviceEvent event = gson.fromJson(record.value(), DeviceEvent.class);
        log.info("[KAFKA] Alert consumed: key={}, type={}", record.key(), event.getEventType());
        processAlert(event);
    }

    // ==================== 直连模式入口 ====================

    @Override
    public void onDeviceEvent(DeviceEvent event) {
        // 直连模式：只处理 STATUS_NOTIFICATION 中的故障
        if (DeviceEvent.STATUS_NOTIFICATION.equals(event.getEventType())) {
            JsonObject payload = gson.fromJson(event.getPayload(), JsonObject.class);
            String status = getStr(payload, "status");
            String errorCode = getStr(payload, "errorCode");

            if ("Faulted".equals(status) && errorCode != null && !"NoError".equals(errorCode)) {
                processAlert(event);
            }
        }
    }

    // ==================== 统一处理逻辑 ====================

    private void processAlert(DeviceEvent event) {
        String chargePointId = event.getChargePointId();
        JsonObject payload = gson.fromJson(event.getPayload(), JsonObject.class);

        String errorCode = getStr(payload, "errorCode");
        String vendorErrorCode = getStr(payload, "vendorErrorCode");
        String info = getStr(payload, "info");
        Integer connectorId = payload.has("connectorId") ? payload.get("connectorId").getAsInt() : null;

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

    private String getStr(JsonObject obj, String key) {
        return obj != null && obj.has(key) && !obj.get(key).isJsonNull()
                ? obj.get(key).getAsString() : null;
    }
}
