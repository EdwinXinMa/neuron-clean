package com.echarge.common.event.kafka;

/**
 * Kafka Topic 常量
 */
public final class KafkaTopics {

    /** 上行：设备生命周期（BOOT_NOTIFICATION / DEVICE_OFFLINE / TOPOLOGY_REPORT） */
    public static final String DEVICE_LIFECYCLE = "device-lifecycle";

    /** 上行：高频遥测（HEARTBEAT / DLM_STATUS / STATUS_NOTIFICATION） */
    public static final String DEVICE_TELEMETRY = "device-telemetry";

    /** 上行：任务进度（FIRMWARE_STATUS） */
    public static final String DEVICE_TASK = "device-task";

    /** 上行：告警事件（由 DeviceEventHandler 二次生产） */
    public static final String DEVICE_ALERT = "device-alert";

    /** 下行：云端指令（预留） */
    public static final String DEVICE_COMMAND = "device-command";

    /** 下行：指令回复（预留） */
    public static final String DEVICE_COMMAND_REPLY = "device-command-reply";

    /** 上行：充电事务（预留） */
    public static final String DEVICE_TRANSACTION = "device-transaction";

    private KafkaTopics() {}
}
