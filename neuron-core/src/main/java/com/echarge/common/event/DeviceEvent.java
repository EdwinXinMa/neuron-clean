package com.echarge.common.event;

import lombok.Data;
import java.io.Serializable;
import java.time.Instant;

/**
 * 设备事件 — protocol 模块发出，device 模块消费
 * @author Edwin
 */
@Data
public class DeviceEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 事件类型 */
    private String eventType;
    /** 设备标识（OCPP chargePointId，通常就是 SN） */
    private String chargePointId;
    /** 事件时间 */
    private String timestamp;
    /** 事件载荷（JSON 字符串） */
    private String payload;

    public DeviceEvent() {}

    public DeviceEvent(String eventType, String chargePointId, String payload) {
        this.eventType = eventType;
        this.chargePointId = chargePointId;
        this.timestamp = Instant.now().toString();
        this.payload = payload;
    }

    /** 事件类型常量 */
    public static final String BOOT_NOTIFICATION = "BOOT_NOTIFICATION";
    public static final String HEARTBEAT = "HEARTBEAT";
    public static final String STATUS_NOTIFICATION = "STATUS_NOTIFICATION";
    public static final String DEVICE_OFFLINE = "DEVICE_OFFLINE";
    public static final String TOPOLOGY_REPORT = "TOPOLOGY_REPORT";
    public static final String DLM_STATUS = "DLM_STATUS";
    public static final String FIRMWARE_STATUS = "FIRMWARE_STATUS";
    public static final String START_TRANSACTION = "START_TRANSACTION";
    public static final String STOP_TRANSACTION = "STOP_TRANSACTION";
}
