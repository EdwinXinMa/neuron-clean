package com.echarge.modules.device.websocket;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * App 端统一 WebSocket 推送通道
 * 连接地址：ws://{host}/app/ws?token={app_token}
 * 单连接 + topic 订阅模式，所有 App 实时推送（OTA进度、设备状态等）共用此通道
 *
 * 客户端订阅：{"action": "subscribe",   "topic": "deviceStatus:9EN03L251119Y0059"}
 * 客户端取消：{"action": "unsubscribe", "topic": "deviceStatus:9EN03L251119Y0059"}
 * 服务端推送：{"topic": "ota:task_ota_001", "status": "DOWNLOADING", "progress": 30, ...}
 */
@Slf4j
@Component
@ServerEndpoint("/app/ws")
public class AppWebSocket {

    /** 全部在线 session */
    private static final CopyOnWriteArraySet<Session> SESSIONS = new CopyOnWriteArraySet<>();

    /** topic → 订阅该 topic 的 sessionId 集合 */
    private static final ConcurrentHashMap<String, Set<String>> TOPIC_SESSIONS = new ConcurrentHashMap<>();

    /** sessionId → session（方便反查） */
    private static final ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    /** sessionId → 该 session 订阅的所有 topic（用于断连时清理） */
    private static final ConcurrentHashMap<String, Set<String>> SESSION_TOPICS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        SESSIONS.add(session);
        SESSION_MAP.put(session.getId(), session);
        SESSION_TOPICS.put(session.getId(), ConcurrentHashMap.newKeySet());
        log.info("[AppWS] 连接建立, sessionId={}, total={}", session.getId(), SESSIONS.size());
    }

    @OnClose
    public void onClose(Session session) {
        String sessionId = session.getId();
        SESSIONS.remove(session);
        SESSION_MAP.remove(sessionId);
        // 清理该 session 的所有 topic 订阅
        Set<String> topics = SESSION_TOPICS.remove(sessionId);
        if (topics != null) {
            for (String topic : topics) {
                Set<String> sessions = TOPIC_SESSIONS.get(topic);
                if (sessions != null) {
                    sessions.remove(sessionId);
                    if (sessions.isEmpty()) {
                        TOPIC_SESSIONS.remove(topic);
                    }
                }
            }
        }
        log.info("[AppWS] 连接关闭, sessionId={}, total={}", sessionId, SESSIONS.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.warn("[AppWS] 连接异常, sessionId={}, err={}", session.getId(), error.getMessage());
        onClose(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JSONObject msg = JSONObject.parseObject(message);
            String action = msg.getString("action");
            String topic = msg.getString("topic");
            if (topic == null || topic.isBlank()) {
                return;
            }
            if ("subscribe".equals(action)) {
                TOPIC_SESSIONS.computeIfAbsent(topic, k -> ConcurrentHashMap.newKeySet()).add(session.getId());
                SESSION_TOPICS.computeIfAbsent(session.getId(), k -> ConcurrentHashMap.newKeySet()).add(topic);
                log.debug("[AppWS] subscribe, sessionId={}, topic={}", session.getId(), topic);
            } else if ("unsubscribe".equals(action)) {
                Set<String> sessions = TOPIC_SESSIONS.get(topic);
                if (sessions != null) {
                    sessions.remove(session.getId());
                }
                Set<String> topics = SESSION_TOPICS.get(session.getId());
                if (topics != null) {
                    topics.remove(topic);
                }
                log.debug("[AppWS] unsubscribe, sessionId={}, topic={}", session.getId(), topic);
            }
        } catch (Exception e) {
            log.warn("[AppWS] 消息解析失败, msg={}, err={}", message, e.getMessage());
        }
    }

    /**
     * 向订阅了指定 topic 的所有 App 推送消息
     * payload 中会自动注入 topic 字段
     *
     * @param topic   topic 名称，如 "deviceStatus:9EN03L251119Y0059" 或 "ota:task_ota_001"
     * @param payload 推送内容（JSONObject），会自动加上 topic 字段
     */
    public static void publish(String topic, JSONObject payload) {
        Set<String> sessionIds = TOPIC_SESSIONS.get(topic);
        if (sessionIds == null || sessionIds.isEmpty()) {
            return;
        }
        payload.put("topic", topic);
        String text = payload.toJSONString();
        for (String sessionId : sessionIds) {
            Session session = SESSION_MAP.get(sessionId);
            if (session != null && session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(text);
                } catch (Exception e) {
                    log.warn("[AppWS] 推送失败, topic={}, sessionId={}, err={}", topic, sessionId, e.getMessage());
                }
            }
        }
        log.info("[AppWS] 推送, topic={}, 订阅数={}", topic, sessionIds.size());
    }
}
