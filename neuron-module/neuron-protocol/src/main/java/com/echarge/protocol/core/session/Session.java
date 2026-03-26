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

    /**
     * 设置会话属性
     * @param key   属性键
     * @param value 属性值
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * 获取会话属性
     * @param key 属性键
     * @param <T> 属性值类型
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * 检查会话是否活跃
     * @return 通道存在且活跃则返回true
     */
    public boolean isActive() {
        return channel != null && channel.isActive();
    }
}
