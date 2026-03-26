package com.echarge.common.event.kafka;

import com.echarge.common.event.DeviceEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 告警事件二次生产者
 * DeviceEventHandler 检测到故障后调用此类，将告警发布到 device-alert topic
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "neuron.kafka.enabled", havingValue = "true")
public class KafkaAlertPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaAlertPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishAlert(DeviceEvent event) {
        String key = event.getChargePointId();
        String value;
        try {
            value = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            log.error("[KAFKA] Failed to serialize alert: {}", e.getMessage());
            return;
        }

        kafkaTemplate.send(KafkaTopics.DEVICE_ALERT, key, value)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[KAFKA] Failed to send alert: key={}, error={}", key, ex.getMessage());
                    } else {
                        log.info("[KAFKA] Alert sent: key={}, partition={}", key,
                                result.getRecordMetadata().partition());
                    }
                });
    }
}
