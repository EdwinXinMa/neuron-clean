package com.echarge.common.constant;

/**
 * 业务常量（按业务领域分区）
 * @author Edwin
 */
public final class BizConstant {

    private BizConstant() {}

    // ==================== 设备状态 ====================

    /** 在线 */
    public static final String DEVICE_ONLINE = "ONLINE";
    /** 离线 */
    public static final String DEVICE_OFFLINE = "OFFLINE";
    /** 故障 */
    public static final String DEVICE_FAULT = "FAULT";
    /** 未激活 */
    public static final String DEVICE_UNACTIVATED = "UNACTIVATED";

    // ==================== OCPP 枪状态 ====================

    /** 可用 */
    public static final String OCPP_AVAILABLE = "Available";
    /** 故障 */
    public static final String OCPP_FAULTED = "Faulted";
    /** 无错误 */
    public static final String OCPP_NO_ERROR = "NoError";
    /** 不可用 */
    public static final String OCPP_UNAVAILABLE = "Unavailable";
    /** 充电中 */
    public static final String OCPP_CHARGING = "Charging";
    /** 暂停（车端） */
    public static final String OCPP_SUSPENDED_EV = "SuspendedEV";
    /** 暂停（桩端） */
    public static final String OCPP_SUSPENDED_EVSE = "SuspendedEVSE";
    /** 准备中 */
    public static final String OCPP_PREPARING = "Preparing";
    /** 结束中 */
    public static final String OCPP_FINISHING = "Finishing";

    // ==================== 固件版本状态 ====================

    /** 草稿 */
    public static final String FIRMWARE_DRAFT = "DRAFT";
    /** 已发布 */
    public static final String FIRMWARE_RELEASED = "RELEASED";
    /** 已废弃 */
    public static final String FIRMWARE_DEPRECATED = "DEPRECATED";

    // ==================== 升级任务状态 ====================

    /** 待执行 */
    public static final String TASK_PENDING = "PENDING";
    /** 下载中 */
    public static final String TASK_DOWNLOADING = "DOWNLOADING";
    /** 安装中 */
    public static final String TASK_INSTALLING = "INSTALLING";
    /** 已完成 */
    public static final String TASK_COMPLETED = "COMPLETED";
    /** 失败 */
    public static final String TASK_FAILED = "FAILED";

    // ==================== DataTransfer 消息ID ====================

    /** 拓扑上报 */
    public static final String DT_TOPOLOGY_REPORT = "TopologyReport";
    /** DLM 状态上报 */
    public static final String DT_DLM_STATUS = "DLMStatus";
    /** DLM 配置下发 */
    public static final String DT_SET_DLM_CONFIG = "SetDLMConfig";

    // ==================== 设备类型 ====================

    /** N3 Lite */
    public static final String TYPE_N3_LITE = "N3_LITE";
    /** ATP III 充电桩 */
    public static final String TYPE_ATP_III = "ATP_III";
}
