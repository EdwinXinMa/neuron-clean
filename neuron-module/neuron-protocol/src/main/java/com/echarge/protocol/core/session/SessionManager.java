package com.echarge.protocol.core.session;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Edwin
 */
@Slf4j
@Component
public class SessionManager {

    private final Map<String, Session> sessionsByChargePointId = new ConcurrentHashMap<>();
    private final Map<Channel, Session> sessionsByChannel = new ConcurrentHashMap<>();

    /**
     * 注册设备会话，若已存在同一chargePointId的会话则先关闭旧会话
     * @param chargePointId   充电点标识（设备SN）
     * @param channel         Netty通道
     * @param protocolVersion OCPP协议版本
     * @return 新创建的会话
     */
    public Session register(String chargePointId, Channel channel, String protocolVersion) {
        // Remove existing session for same chargePointId
        Session existing = sessionsByChargePointId.get(chargePointId);
        if (existing != null) {
            log.warn("Charge point {} already connected, closing old session", chargePointId);
            unregister(existing.getChannel());
        }

        String sessionId = UUID.randomUUID().toString();
        Session session = new Session(sessionId, chargePointId, channel, protocolVersion);
        sessionsByChargePointId.put(chargePointId, session);
        sessionsByChannel.put(channel, session);
        log.info("Session registered: chargePointId={}, protocol={}, sessionId={}", chargePointId, protocolVersion, sessionId);
        return session;
    }

    /**
     * 注销设备会话
     * @param channel Netty通道
     */
    public void unregister(Channel channel) {
        Session session = sessionsByChannel.remove(channel);
        if (session != null) {
            sessionsByChargePointId.remove(session.getChargePointId());
            log.info("Session unregistered: chargePointId={}", session.getChargePointId());
        }
    }

    /**
     * 根据Netty通道获取会话
     * @param channel Netty通道
     * @return 会话对象，不存在则返回null
     */
    public Session getByChannel(Channel channel) {
        return sessionsByChannel.get(channel);
    }

    /**
     * 根据充电点标识获取会话
     * @param chargePointId 充电点标识（设备SN）
     * @return 会话对象，不存在则返回null
     */
    public Session getByChargePointId(String chargePointId) {
        return sessionsByChargePointId.get(chargePointId);
    }

    /**
     * 获取所有活跃会话
     * @return 会话集合
     */
    public Collection<Session> getAll() {
        return sessionsByChargePointId.values();
    }

    /**
     * 向指定充电点发送WebSocket消息
     * @param chargePointId 充电点标识（设备SN）
     * @param message       消息内容
     */
    public void sendMessage(String chargePointId, String message) {
        Session session = sessionsByChargePointId.get(chargePointId);
        if (session != null && session.isActive()) {
            session.getChannel().writeAndFlush(new TextWebSocketFrame(message));
        } else {
            log.warn("Cannot send message to {}: session not found or inactive", chargePointId);
        }
    }
}
