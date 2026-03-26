package com.echarge.common.event;

/**
 * 设备事件发布接口
 * Kafka 开启时走 Kafka，关闭时走 Spring 事件直连
 * @author Edwin
 */
public interface DeviceEventPublisher {

    /**
     * 发布设备事件
     * @param event 设备事件对象
     */
    void publish(DeviceEvent event);
}
