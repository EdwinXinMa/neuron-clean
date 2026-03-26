package com.echarge.protocol.core.handler;

import com.echarge.protocol.core.session.Session;

/**
 * @author Edwin
 */
public interface ActionHandler<REQ, RESP> {

    /**
     * 该处理器对应的OCPP动作名称（如 "BootNotification"）
     * @return 动作名称
     */
    String action();

    /**
     * 请求类型的Class对象，用于Gson反序列化
     * @return 请求类型Class
     */
    Class<REQ> requestType();

    /**
     * 处理请求并返回响应
     * @param session 当前设备会话
     * @param request 请求对象
     * @return 响应对象
     */
    RESP handle(Session session, REQ request);
}
