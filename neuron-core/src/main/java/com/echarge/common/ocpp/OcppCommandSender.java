package com.echarge.common.ocpp;

/**
 * OCPP 命令下发接口 — 定义在 core 层，protocol 模块实现，device 模块调用
 * @author Edwin
 */
public interface OcppCommandSender {

    /**
     * 检查设备 OCPP 会话是否存在且活跃
     * @param chargePointId 充电点标识（设备SN）
     * @return 是否已连接
     */
    boolean isDeviceConnected(String chargePointId);

    /**
     * 向设备发送 OCPP CALL 消息（JSON 字符串）
     * @param chargePointId 充电点标识（设备SN）
     * @param message       OCPP消息JSON字符串
     */
    void sendCall(String chargePointId, String message);

    /**
     * 向设备发送 OCPP CALL 并同步等待 CALLRESULT 响应
     * @param chargePointId  充电点标识（设备SN）
     * @param message        OCPP消息JSON字符串
     * @param messageId      消息ID，用于匹配响应
     * @param timeoutSeconds 超时秒数
     * @return 响应 payload JSON 字符串，超时返回 null
     */
    String sendCallAndWait(String chargePointId, String message, String messageId, long timeoutSeconds);
}
