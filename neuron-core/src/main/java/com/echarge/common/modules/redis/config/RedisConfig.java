package com.echarge.common.modules.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Resource
    private NeuronRedisCacheTtls redisCacheProperties;

    private static volatile Jackson2JsonRedisSerializer<Object> cachedJacksonSerializer;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        long startTime = System.currentTimeMillis();
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = getJacksonSerializer();
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        long endTime = System.currentTimeMillis();
        log.info(" --- redis config init ---，耗时: {}ms", (endTime - startTime));
        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory factory) {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = getJacksonSerializer();
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(6L));
        RedisCacheConfiguration redisCacheConfiguration = config
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer));
        NeuronRedisCacheWriter writer = new NeuronRedisCacheWriter(factory, Duration.ofMillis(50L));
        HashMap<String, RedisCacheConfiguration> initialCaches = new HashMap<>();
        Map<String, Long> cacheTtls = this.redisCacheProperties.getCacheTtls();
        if (cacheTtls != null && !cacheTtls.isEmpty()) {
            cacheTtls.forEach((cacheName, ttl) -> {
                log.debug("自定义缓存配置，cacheKey:{}, 缓存秒数:{}", cacheName, ttl);
                initialCaches.put(cacheName, RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofSeconds(ttl))
                        .disableCachingNullValues()
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer)));
            });
        }
        RedisConfigCacheManager cacheManager = new RedisConfigCacheManager(writer, redisCacheConfiguration, initialCaches);
        cacheManager.setTransactionAware(true);
        return cacheManager;
    }

    @Bean
    @ConditionalOnProperty(prefix = "neuron.redis", name = "listener-enabled", havingValue = "true", matchIfMissing = true)
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory redisConnectionFactory, RedisReceiver redisReceiver, MessageListenerAdapter commonListenerAdapter) {
        log.info("Redis消息监听器已启用。如果Redis不支持SUBSCRIBE命令，请设置 neuron.redis.listener-enabled=false");
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(commonListenerAdapter, new ChannelTopic("neuron_redis_topic"));
        return container;
    }

    @Bean
    @ConditionalOnProperty(prefix = "neuron.redis", name = "listener-enabled", havingValue = "true", matchIfMissing = true)
    MessageListenerAdapter commonListenerAdapter(RedisReceiver redisReceiver) {
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(redisReceiver, "onMessage");
        messageListenerAdapter.setSerializer(getJacksonSerializer());
        return messageListenerAdapter;
    }

    private static Jackson2JsonRedisSerializer<Object> getJacksonSerializer() {
        if (cachedJacksonSerializer != null) {
            return cachedJacksonSerializer;
        }
        synchronized (RedisConfig.class) {
            if (cachedJacksonSerializer != null) {
                return cachedJacksonSerializer;
            }
            cachedJacksonSerializer = jacksonSerializer();
            return cachedJacksonSerializer;
        }
    }

    private static Jackson2JsonRedisSerializer<Object> jacksonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }

    public static class RedisConfigCacheManager extends RedisCacheManager {
        private static final RedisSerializationContext.SerializationPair<Object> DEFAULT_PAIR =
                RedisSerializationContext.SerializationPair.fromSerializer(RedisConfig.jacksonSerializer());
        private static final CacheKeyPrefix DEFAULT_CACHE_KEY_PREFIX = cacheName -> cacheName + "::";

        public RedisConfigCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration, Map<String, RedisCacheConfiguration> initialCaches) {
            super(cacheWriter, defaultCacheConfiguration, initialCaches, true);
        }

        @Override
        protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
            int lastIndexOf = name.lastIndexOf('#');
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
