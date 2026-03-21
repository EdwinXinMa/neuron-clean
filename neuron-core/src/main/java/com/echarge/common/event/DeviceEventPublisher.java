package com.echarge.common.event;

/**
 * 设备事件发布接口
 * Kafka 开启时走 Kafka，关闭时走 Spring 事件直连
 */
public interface DeviceEventPublisher {
    void publish(DeviceEvent event);
}
