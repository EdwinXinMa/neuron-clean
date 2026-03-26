package com.echarge.protocol.core.session;

import io.netty.channel.Channel;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Edwin
 */
@Data
public class Session {

    private final String id;
    private final String chargePointId;
    private final Channel channel;
    private final String protocolVersion;
    private final Instant createTime;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public Session(String id, String chargePointId, Channel channel, String protocolVersion) {
        this.id = id;
        this.chargePointId = chargePointId;
        this.channel = channel;
        this.protocolVersion = protocolVersion;
        this.createTime = Instant.now();
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    public boolean isActive() {
        return channel != null && channel.isActive();
    }
}
