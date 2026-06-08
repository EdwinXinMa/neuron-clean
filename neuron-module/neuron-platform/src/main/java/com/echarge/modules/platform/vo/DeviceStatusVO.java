package com.echarge.modules.platform.vo;

import lombok.Data;

/**
 * 设备在线状态响应
 *
 * @author Edwin
 */
@Data
public class DeviceStatusVO {

    /** 设备序列号 */
    private String sn;

    /** 在线状态：ONLINE / OFFLINE / UNACTIVATED */
    private String onlineStatus;

    /** 固件版本（可为空） */
    private String firmwareVersion;
}
