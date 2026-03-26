package com.echarge.protocol.core.dispatcher;

import com.echarge.protocol.core.session.Session;

/**
 * @author Edwin
 */
public interface MessageDispatcher {

    /**
     * 将入站CALL消息分发到对应的处理器
     * @param session 当前设备会话
     * @param message 入站消息
     * @return 出站响应消息
     */
    OutboundMessage dispatch(Session session, InboundMessage message);

    /**
     * 该分发器支持的OCPP子协议（如 "ocpp1.6"、"ocpp2.0.1"）
     * @return 协议版本标识
     */
    String supportedProtocol();
}
