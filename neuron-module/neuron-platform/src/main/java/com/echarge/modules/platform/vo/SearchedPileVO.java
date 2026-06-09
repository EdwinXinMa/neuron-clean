package com.echarge.modules.platform.vo;

import lombok.Data;

/**
 * 搜索到的充电桩信息（固件 PLC 扫描结果）
 *
 * @author Edwin
 */
@Data
public class SearchedPileVO {

    /** 桩自身 SN */
    private String subDevId;

    /** 复合序列号：{pileSN}_{gatewaySN}，H 平台入库时用作 serial_no */
    private String deviceSn;

    /** 设备类型，充电桩固定为 "ev" */
    private String productType;

    /** 品牌 */
    private String deviceBrand;

    /** 型号 ID */
    private String modelId;

    /** 固件版本 */
    private String modelVersion;

    /** 产品 ID */
    private String productId;

    /** PLC 连接状态：online / offline */
    private String connectStatus;

    /** 充电枪数量（1 或 2） */
    private int connectorCount;

    /** PLC 设备留空 */
    private String portName;

    /** PLC 设备留空 */
    private String protocolAddr;
}
