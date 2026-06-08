package com.echarge.modules.platform.callback;

import com.echarge.common.event.DeviceEvent;
import com.echarge.common.event.DeviceEventListener;
import com.echarge.common.event.kafka.KafkaTopics;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * H 平台回调事件监听器
 *
 * <p>监听 N 平台的设备生命周期事件，将 BOOT_NOTIFICATION（上线）和 DEVICE_OFFLINE（下线）
 * 推送到 H 平台，使 H 平台能实时更新 N3Lite 网关在线状态。
 *
 * <ul>
 *   <li>Kafka 模式：消费 {@link KafkaTopics#DEVICE_LIFECYCLE} topic</li>
 *   <li>直连模式：由 {@link com.echarge.common.event.LocalDeviceEventPublisher} 直接回调</li>
 * </ul>
 */
@Slf4j
@Component
public class HemsCallbackListener implements DeviceEventListener {

    private final HemsCallbackClient callbackClient;
    private final Gson gson = new Gson();

    public HemsCallbackListener(HemsCallbackClient callbackClient) {
        this.callbackClient = callbackClient;
    }

    // ==================== Kafka 消费入口 ====================

    @KafkaListener(
            topics = KafkaTopics.DEVICE_LIFECYCLE,
            groupId = "hems-callback-handler",
            autoStartup = "${neuron.kafka.enabled:false}"
    )
    public void onLifecycleMessage(ConsumerRecord<String, String> record) {
        DeviceEvent event = gson.fromJson(record.value(), DeviceEvent.class);
        log.debug("【H平台回调-Kafka】type={} sn={}", event.getEventType(), event.getChargePointId());
        handleEvent(event);
    }

    // ==================== 直连模式入口 ====================

    @Override
    public void onDeviceEvent(DeviceEvent event) {
        handleEvent(event);
    }

    // ==================== 统一处理逻辑 ====================

    private void handleEvent(DeviceEvent event) {
        String sn = event.getChargePointId();
        switch (event.getEventType()) {
            case DeviceEvent.BOOT_NOTIFICATION:
                String firmwareVersion = parseFirmwareVersion(event.getPayload());
                callbackClient.sendEvent("DEVICE_ONLINE", sn, firmwareVersion);
                break;
            case DeviceEvent.DEVICE_OFFLINE:
                callbackClient.sendEvent("DEVICE_OFFLINE", sn, null);
                break;
            default:
                break;
        }
    }

    private String parseFirmwareVersion(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            JsonObject obj = gson.fromJson(payload, JsonObject.class);
            return obj.has("firmwareVersion") && !obj.get("firmwareVersion").isJsonNull()
                    ? obj.get("firmwareVersion").getAsString() : null;
        } catch (Exception e) {
            log.warn("【H平台回调】解析 firmwareVersion 失败: {}", e.getMessage());
            return null;
        }
    }
}
