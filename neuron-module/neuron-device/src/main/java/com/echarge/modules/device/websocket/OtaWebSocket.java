package com.echarge.modules.device.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Edwin
 */
@Slf4j
@Component
@ServerEndpoint("/otaSocket/{token}")
public class OtaWebSocket {

    private static final ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        SESSION_MAP.put(token, session);
        log.info("OTA WebSocket 连接建立, token={}", token);
    }

    @OnClose
    public void onClose(@PathParam("token") String token) {
        SESSION_MAP.remove(token);
        log.info("OTA WebSocket 连接关闭, token={}", token);
    }

    @OnError
    public void onError(@PathParam("token") String token, Throwable error) {
        SESSION_MAP.remove(token);
        log.error("OTA WebSocket 异常, token={}", token, error);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("token") String token) {
        log.debug("OTA WebSocket 收到消息, token={}, message={}", token, message);
    }

    /**
     * 向指定 token（设备SN）推送消息
     */
    public static void sendMessage(String token, String message) {
        Session session = SESSION_MAP.get(token);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("OTA WebSocket 推送失败, token={}", token, e);
            }
        }
    }
}
