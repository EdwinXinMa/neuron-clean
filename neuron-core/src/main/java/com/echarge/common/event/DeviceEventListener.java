package com.echarge.common.event;

/**
 * 设备事件监听接口
 * device 模块实现此接口来处理设备事件
 * @author Edwin
 */
public interface DeviceEventListener {

    /**
     * 处理设备事件
     * @param event 设备事件对象
     */
    void onDeviceEvent(DeviceEvent event);
}
