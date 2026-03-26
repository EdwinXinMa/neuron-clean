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

    public void unregister(Channel channel) {
        Session session = sessionsByChannel.remove(channel);
        if (session != null) {
            sessionsByChargePointId.remove(session.getChargePointId());
            log.info("Session unregistered: chargePointId={}", session.getChargePointId());
        }
    }

    public Session getByChannel(Channel channel) {
        return sessionsByChannel.get(channel);
    }

    public Session getByChargePointId(String chargePointId) {
        return sessionsByChargePointId.get(chargePointId);
    }

    public Collection<Session> getAll() {
        return sessionsByChargePointId.values();
    }

    public void sendMessage(String chargePointId, String message) {
        Session session = sessionsByChargePointId.get(chargePointId);
        if (session != null && session.isActive()) {
            session.getChannel().writeAndFlush(new TextWebSocketFrame(message));
        } else {
            log.warn("Cannot send message to {}: session not found or inactive", chargePointId);
        }
    }
}
