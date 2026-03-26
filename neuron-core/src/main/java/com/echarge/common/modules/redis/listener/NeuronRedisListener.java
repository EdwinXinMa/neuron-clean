package com.echarge.common.modules.redis.listener;

import com.echarge.common.base.BaseMap;

/**
 * @author Edwin
 */
public interface NeuronRedisListener {
    /**
     * 处理 Redis 消息
     * @param var1 消息内容
     */
    void onMessage(BaseMap var1);
}

