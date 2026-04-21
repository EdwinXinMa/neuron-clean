package com.echarge.modules.device.websocket;

import com.echarge.common.websocket.FrontendPushChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 设备事件 WebSocket — 广播设备状态变化到所有前端客户端
 * 前端连接 /deviceSocket 后，设备上线/离线/故障/告警 等事件会实时推送
 * @author Edwin
 */
@Slf4j
@Component
@ServerEndpoint("/deviceSocket")
public class DeviceEventWebSocket {

    private static final CopyOnWriteArraySet<Session> SESSIONS = new CopyOnWriteArraySet<>();

    // 静态初始化注册广播通道
    static {
        FrontendPushChannel.register(DeviceEventWebSocket::broadcast);
    }

    @OnOpen
    public void onOpen(Session session) {
        SESSIONS.add(session);
        log.info("DeviceEventWebSocket connected, total={}", SESSIONS.size());
    }

    @OnClose
    public void onClose(Session session) {
        SESSIONS.remove(session);
        log.info("DeviceEventWebSocket closed, total={}", SESSIONS.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        SESSIONS.remove(session);
        log.warn("DeviceEventWebSocket disconnected: {}", error.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 前端不需要发消息，忽略
    }

    public static void broadcast(String message) {
        for (Session session : SESSIONS) {
            if (session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(message);
                } catch (Exception e) {
                    log.warn("DeviceEventWebSocket broadcast failed: {}", e.getMessage());
                }
            }
        }
    }
}
