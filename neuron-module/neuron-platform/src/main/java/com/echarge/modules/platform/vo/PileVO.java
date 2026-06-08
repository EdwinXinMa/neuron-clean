package com.echarge.modules.platform.vo;

import lombok.Data;

/**
 * 下挂充电桩信息
 *
 * @author Edwin
 */
@Data
public class PileVO {

    /** 桩自身 SN */
    private String subDevId;

    /** 复合序列号：{pileSN}_{gatewaySN}，与 HEMS sub_devices.serial_no 对齐 */
    private String deviceSn;

    /** 设备类型（ATP_III 等） */
    private String deviceType;

    /** 具体型号 */
    private String deviceModel;

    /** 固件版本 */
    private String firmwareVersion;

    /** 在线状态：ONLINE / OFFLINE / UNACTIVATED */
    private String onlineStatus;
}
