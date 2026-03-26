/*
 * 
 * Could not load the following classes:
 *  jakarta.annotation.Resource
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.data.redis.core.RedisTemplate
 */
package com.echarge.common.modules.redis.client;

import jakarta.annotation.Resource;
import com.echarge.common.base.BaseMap;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author Edwin
 */
@Configuration
public class NeuronRedisClient {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public void sendMessage(String handlerName, BaseMap params) {
        params.put("handlerName", handlerName);
        this.redisTemplate.convertAndSend("neuron_redis_topic", params);
    }
}

