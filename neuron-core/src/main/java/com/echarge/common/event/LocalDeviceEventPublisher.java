package com.echarge.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 本地直连模式 — Kafka 未开启时，直接调用 DeviceEventListener
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "neuron.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class LocalDeviceEventPublisher implements DeviceEventPublisher {

    @Autowired(required = false)
    private List<DeviceEventListener> listeners;

    @Override
    public void publish(DeviceEvent event) {
        log.debug("[LOCAL] Publishing event: type={}, chargePointId={}", event.getEventType(), event.getChargePointId());
        if (listeners != null) {
            for (DeviceEventListener listener : listeners) {
                try {
                    listener.onDeviceEvent(event);
                } catch (Exception e) {
                    log.error("[LOCAL] Error processing event: {}", e.getMessage(), e);
                }
            }
        }
    }
}
