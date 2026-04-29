package com.echarge.modules.device.websocket;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * App 端设备状态实时推送 WebSocket
 * 连接地址：ws://{host}/app/deviceStatus/{sn}?token={app_token}
 * 设备上线/离线时向订阅该 SN 的 App 推送状态变化
 */
@Slf4j
@Component
@ServerEndpoint("/app/deviceStatus/{sn}")
public class AppDeviceStatusWebSocket {

    /** sn → Session（每台设备同时可能有多个 App 连接） */
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Session>> SN_SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("sn") String sn) {
        SN_SESSIONS.computeIfAbsent(sn, k -> new ConcurrentHashMap<>()).put(session.getId(), session);
        log.info("[AppDeviceStatus] 连接建立, sn={}, sessionId={}", sn, session.getId());
    }

    @OnClose
    public void onClose(Session session, @PathParam("sn") String sn) {
        ConcurrentHashMap<String, Session> sessions = SN_SESSIONS.get(sn);
        if (sessions != null) {
            sessions.remove(session.getId());
            if (sessions.isEmpty()) {
                SN_SESSIONS.remove(sn);
            }
        }
        log.info("[AppDeviceStatus] 连接关闭, sn={}, sessionId={}", sn, session.getId());
    }

    @OnError
    public void onError(Session session, @PathParam("sn") String sn, Throwable error) {
        ConcurrentHashMap<String, Session> sessions = SN_SESSIONS.get(sn);
        if (sessions != null) {
            sessions.remove(session.getId());
        }
        log.warn("[AppDeviceStatus] 连接异常, sn={}, err={}", sn, error.getMessage());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("sn") String sn) {
        // App 端只监听，不需要处理上行消息
    }

    /**
     * 向订阅指定 SN 的所有 App 推送设备状态
     *
     * @param sn      设备 SN
     * @param status  状态：ONLINE / OFFLINE
     * @param message 说明文字
     */
    public static void push(String sn, String status, String message) {
        ConcurrentHashMap<String, Session> sessions = SN_SESSIONS.get(sn);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        JSONObject payload = new JSONObject();
        payload.put("sn", sn);
        payload.put("status", status);
        payload.put("message", message);
        String text = payload.toJSONString();

        for (Session session : sessions.values()) {
            if (session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(text);
                } catch (Exception e) {
                    log.warn("[AppDeviceStatus] 推送失败, sn={}, err={}", sn, e.getMessage());
                }
            }
        }
        log.info("[AppDeviceStatus] 推送, sn={}, status={}", sn, status);
    }
}
