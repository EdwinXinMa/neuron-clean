/*
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonAutoDetect$Visibility
 *  com.fasterxml.jackson.annotation.PropertyAccessor
 *  com.fasterxml.jackson.databind.DeserializationFeature
 *  com.fasterxml.jackson.databind.Module
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  com.fasterxml.jackson.databind.ObjectMapper$DefaultTyping
 *  com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
 *  jakarta.annotation.Resource
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.cache.CacheManager
 *  org.springframework.cache.annotation.CachingConfigurerSupport
 *  org.springframework.cache.annotation.EnableCaching
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.data.redis.cache.CacheKeyPrefix
 *  org.springframework.data.redis.cache.RedisCache
 *  org.springframework.data.redis.cache.RedisCacheConfiguration
 *  org.springframework.data.redis.cache.RedisCacheManager
 *  org.springframework.data.redis.cache.RedisCacheWriter
 *  org.springframework.data.redis.connection.MessageListener
 *  org.springframework.data.redis.connection.RedisConnectionFactory
 *  org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
 *  org.springframework.data.redis.core.RedisTemplate
 *  org.springframework.data.redis.listener.ChannelTopic
 *  org.springframework.data.redis.listener.RedisMessageListenerContainer
 *  org.springframework.data.redis.listener.Topic
 *  org.springframework.data.redis.listener.adapter.MessageListenerAdapter
 *  org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
 *  org.springframework.data.redis.serializer.RedisSerializationContext$SerializationPair
 *  org.springframework.data.redis.serializer.RedisSerializer
 *  org.springframework.data.redis.serializer.StringRedisSerializer
 */
package com.echarge.common.modules.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import com.echarge.common.modules.redis.config.NeuronRedisCacheTtls;
import com.echarge.common.modules.redis.receiver.RedisReceiver;
import com.echarge.common.modules.redis.writer.NeuronRedisCacheWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching
@Configuration
public class RedisConfig
extends CachingConfigurerSupport {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);
    @Resource
    private LettuceConnectionFactory lettuceConnectionFactory;
    @Resource
    private NeuronRedisCacheTtls redisCacheProperties;
    private static volatile Jackson2JsonRedisSerializer<Object> cachedJacksonSerializer;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        long startTime = System.currentTimeMillis();
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = RedisConfig.getJacksonSerializer();
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory((RedisConnectionFactory)lettuceConnectionFactory);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer((RedisSerializer)stringSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer((RedisSerializer)stringSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        long endTime = System.currentTimeMillis();
        log.info(" --- redis config init ---\uff0c\u8017\u65f6: {}ms", (endTime - startTime));
        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory factory) {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = RedisConfig.getJacksonSerializer();
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(6L));
        RedisCacheConfiguration redisCacheConfiguration = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer((RedisSerializer)new StringRedisSerializer())).serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer));
        NeuronRedisCacheWriter writer = new NeuronRedisCacheWriter((RedisConnectionFactory)factory, Duration.ofMillis(50L));
        HashMap<String, RedisCacheConfiguration> initialCaches = new HashMap<String, RedisCacheConfiguration>();
        initialCaches.put("sys:cache:dictTable", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10L)).disableCachingNullValues().serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer)));
        initialCaches.put("test:demo", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5L)).disableCachingNullValues());
        initialCaches.put("pluginMall::rankingList", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(24L)).disableCachingNullValues());
        initialCaches.put("pluginMall::queryPageList", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(24L)).disableCachingNullValues());
        initialCaches.put("flow:runtimeData", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(365L)).disableCachingNullValues());
        Map<String, Long> cacheTtls = this.redisCacheProperties.getCacheTtls();
        if (cacheTtls != null && !cacheTtls.isEmpty()) {
            cacheTtls.forEach((cacheName, ttl) -> {
                log.debug("\u81ea\u5b9a\u4e49\u7f13\u5b58\u914d\u7f6e\uff0ccacheKey:{}, \u7f13\u5b58\u79d2\u6570:{}", cacheName, ttl);
                initialCaches.put((String)cacheName, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(ttl)).disableCachingNullValues().serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer((RedisSerializer)jackson2JsonRedisSerializer)));
            });
        }
        RedisConfigCacheManager cacheManager = new RedisConfigCacheManager(writer, redisCacheConfiguration, initialCaches);
        cacheManager.setTransactionAware(true);
        return cacheManager;
    }

    @Bean
    @ConditionalOnProperty(prefix="neuron.redis", name={"listener-enabled"}, havingValue="true", matchIfMissing=true)
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory redisConnectionFactory, RedisReceiver redisReceiver, MessageListenerAdapter commonListenerAdapter) {
        log.info("Redis\u6d88\u606f\u76d1\u542c\u5668\u5df2\u542f\u7528\u3002\u5982\u679cRedis\u4e0d\u652f\u6301SUBSCRIBE\u547d\u4ee4\uff0c\u8bf7\u8bbe\u7f6e neuron.redis.listener-enabled=false");
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener((MessageListener)commonListenerAdapter, (Topic)new ChannelTopic("neuron_redis_topic"));
        return container;
    }

    @Bean
    @ConditionalOnProperty(prefix="neuron.redis", name={"listener-enabled"}, havingValue="true", matchIfMissing=true)
    MessageListenerAdapter commonListenerAdapter(RedisReceiver redisReceiver) {
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(redisReceiver, "onMessage");
        messageListenerAdapter.setSerializer(RedisConfig.jacksonSerializer());
        return messageListenerAdapter;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static Jackson2JsonRedisSerializer<Object> getJacksonSerializer() {
        if (cachedJacksonSerializer != null) {
            return cachedJacksonSerializer;
        }
        Class<RedisConfig> clazz = RedisConfig.class;
        synchronized (RedisConfig.class) {
            if (cachedJacksonSerializer != null) {
                return cachedJacksonSerializer;
            }
            cachedJacksonSerializer = RedisConfig.jacksonSerializer();
            // ** MonitorExit[var0] (shouldn't be in output)
            return cachedJacksonSerializer;
        }
    }

    private static Jackson2JsonRedisSerializer<Object> jacksonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.registerModule((Module)new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }

    public static class RedisConfigCacheManager
    extends RedisCacheManager {
        private static final RedisSerializationContext.SerializationPair<Object> DEFAULT_PAIR = RedisSerializationContext.SerializationPair.fromSerializer(RedisConfig.jacksonSerializer());
        private static final CacheKeyPrefix DEFAULT_CACHE_KEY_PREFIX = cacheName -> cacheName + "::";

        public RedisConfigCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration, Map<String, RedisCacheConfiguration> initialCaches) {
            super(cacheWriter, defaultCacheConfiguration, initialCaches, true);
        }

        protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
            int lastIndexOf = name.lastIndexOf(35);
            if (lastIndexOf > -1) {
                String ttl = name.substring(lastIndexOf + 1);
                Duration duration = Duration.ofSeconds(Long.parseLong(ttl));
                cacheConfig = cacheConfig.entryTtl(duration);
                cacheConfig = cacheConfig.computePrefixWith(DEFAULT_CACHE_KEY_PREFIX).serializeValuesWith(DEFAULT_PAIR);
                String cacheName = name.substring(0, lastIndexOf);
                return super.createRedisCache(cacheName, cacheConfig);
            }
            cacheConfig = cacheConfig.computePrefixWith(DEFAULT_CACHE_KEY_PREFIX).serializeValuesWith(DEFAULT_PAIR);
            return super.createRedisCache(name, cacheConfig);
        }
    }
}

