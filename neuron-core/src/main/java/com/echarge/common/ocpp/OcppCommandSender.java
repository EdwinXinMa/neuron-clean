package com.echarge.common.ocpp;

/**
 * OCPP 命令下发接口 — 定义在 core 层，protocol 模块实现，device 模块调用
 * @author Edwin
 */
public interface OcppCommandSender {

    /**
     * 检查设备 OCPP 会话是否存在且活跃
     */
    boolean isDeviceConnected(String chargePointId);

    /**
     * 向设备发送 OCPP CALL 消息（JSON 字符串）
     */
    void sendCall(String chargePointId, String message);
}
