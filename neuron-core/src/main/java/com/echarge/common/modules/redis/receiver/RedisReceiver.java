/*
 * 
 * Could not load the following classes:
 *  cn.hutool.core.util.ObjectUtil
 *  lombok.Generated
 *  org.springframework.stereotype.Component
 */
package com.echarge.common.modules.redis.receiver;

import cn.hutool.core.util.ObjectUtil;
import lombok.Generated;
import com.echarge.common.base.BaseMap;
import com.echarge.common.modules.redis.listener.NeuronRedisListener;
import com.echarge.common.util.SpringContextHolder;
import org.springframework.stereotype.Component;

@Component
public class RedisReceiver {
    public void onMessage(BaseMap params) {
        Object handlerName = params.get("handlerName");
        NeuronRedisListener messageListener = SpringContextHolder.getHandler(handlerName.toString(), NeuronRedisListener.class);
        if (ObjectUtil.isNotEmpty(messageListener)) {
            messageListener.onMessage(params);
        }
    }

    @Generated
    public RedisReceiver() {
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof RedisReceiver)) {
            return false;
        }
        RedisReceiver other = (RedisReceiver)o;
        return other.canEqual(this);
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof RedisReceiver;
    }

    @Generated
    public int hashCode() {
        boolean result = true;
        return 1;
    }

    @Generated
    public String toString() {
        return "RedisReceiver()";
    }
}

