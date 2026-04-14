package com.echarge.modules.device.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * App 端 OTA 升级进度 WebSocket
 * 连接地址：ws://{host}/app/ota/{taskId}?token={app_token}
 * @author Edwin
 */
@Slf4j
@Component
@ServerEndpoint("/app/ota/{taskId}")
public class AppOtaWebSocket {

    private static final ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("taskId") String taskId) {
        SESSION_MAP.put(taskId, session);
        log.info("[AppOTA] WebSocket 连接建立, taskId={}", taskId);
    }

    @OnClose
    public void onClose(@PathParam("taskId") String taskId) {
        SESSION_MAP.remove(taskId);
        log.info("[AppOTA] WebSocket 连接关闭, taskId={}", taskId);
    }

    @OnError
    public void onError(@PathParam("taskId") String taskId, Throwable error) {
        SESSION_MAP.remove(taskId);
        log.error("[AppOTA] WebSocket 异常, taskId={}", taskId, error);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("taskId") String taskId) {
        log.debug("[AppOTA] 收到消息, taskId={}, message={}", taskId, message);
    }

    /**
     * 向指定 taskId 推送进度消息
     */
    public static void sendMessage(String taskId, String message) {
        Session session = SESSION_MAP.get(taskId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("[AppOTA] 推送失败, taskId={}", taskId, e);
            }
        }
    }

    /**
     * 检查指定 taskId 是否有 App 端 WebSocket 连接
     */
    public static boolean hasConnection(String taskId) {
        Session session = SESSION_MAP.get(taskId);
        return session != null && session.isOpen();
    }
}
