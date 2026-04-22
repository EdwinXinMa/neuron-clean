package com.echarge.modules.device.websocket;

import com.echarge.common.websocket.FrontendPushChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 设备事件 WebSocket — 广播设备状态变化到所有前端客户端
 * 前端连接 /deviceSocket 后，设备上线/离线/故障/告警 等事件会实时推送
 * 前端发送 SN 订阅某设备的 DLMStatus 实时数据推送
 * @author Edwin
 */
@Slf4j
@Component
@ServerEndpoint("/deviceSocket")
public class DeviceEventWebSocket {

    private static final CopyOnWriteArraySet<Session> SESSIONS = new CopyOnWriteArraySet<>();

    /** Session → 订阅的设备 SN（前端发送 SN 来订阅） */
    private static final ConcurrentHashMap<Session, String> SUBSCRIPTIONS = new ConcurrentHashMap<>();

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
        SUBSCRIPTIONS.remove(session);
        log.info("DeviceEventWebSocket closed, total={}", SESSIONS.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        SESSIONS.remove(session);
        SUBSCRIPTIONS.remove(session);
        log.warn("DeviceEventWebSocket disconnected: {}", error.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 前端发送设备 SN 来订阅 DLMStatus 实时推送
        String sn = message.trim();
        if (!sn.isEmpty()) {
            SUBSCRIPTIONS.put(session, sn);
            log.debug("DeviceEventWebSocket subscribe: session={}, sn={}", session.getId(), sn);
        }
    }

    /**
     * 广播消息到所有前端（设备上线/离线/故障/告警）
     */
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

    /**
     * 推送 DLMStatus 数据给订阅了指定 SN 的前端 Session
     */
    public static void pushToSubscribers(String sn, String message) {
        for (var entry : SUBSCRIPTIONS.entrySet()) {
            if (sn.equals(entry.getValue()) && entry.getKey().isOpen()) {
                try {
                    entry.getKey().getAsyncRemote().sendText(message);
                } catch (Exception e) {
                    log.warn("DeviceEventWebSocket push failed: sn={}, err={}", sn, e.getMessage());
                }
            }
        }
    }
}
