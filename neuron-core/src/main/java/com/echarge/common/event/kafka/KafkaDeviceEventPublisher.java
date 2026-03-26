package com.echarge.common.event.kafka;

import com.echarge.common.event.DeviceEvent;
import com.echarge.common.event.DeviceEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka 模式 — 按事件类型路由到不同 Topic，chargePointId 作为 partition key
 * @author Edwin
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "neuron.kafka.enabled", havingValue = "true")
public class KafkaDeviceEventPublisher implements DeviceEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaDeviceEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DeviceEvent event) {
        String topic = resolveTopic(event.getEventType());
        String key = event.getChargePointId();
        String value;
        try {
            value = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            log.error("[KAFKA] Failed to serialize event: {}", e.getMessage());
            return;
        }

        kafkaTemplate.send(topic, key, value)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[KAFKA] Failed to send event: topic={}, key={}, error={}",
                                topic, key, ex.getMessage());
                    } else {
                        log.debug("[KAFKA] Event sent: topic={}, key={}, partition={}, offset={}",
                                topic, key,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * 按事件类型路由到对应 Topic
     */
    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case DeviceEvent.BOOT_NOTIFICATION,
                 DeviceEvent.DEVICE_OFFLINE,
                 DeviceEvent.TOPOLOGY_REPORT -> KafkaTopics.DEVICE_LIFECYCLE;

            case DeviceEvent.HEARTBEAT,
                 DeviceEvent.DLM_STATUS,
                 DeviceEvent.STATUS_NOTIFICATION -> KafkaTopics.DEVICE_TELEMETRY;

            case DeviceEvent.FIRMWARE_STATUS -> KafkaTopics.DEVICE_TASK;

            default -> {
                log.warn("[KAFKA] Unknown event type: {}, routing to lifecycle", eventType);
                yield KafkaTopics.DEVICE_LIFECYCLE;
            }
        };
    }
}
