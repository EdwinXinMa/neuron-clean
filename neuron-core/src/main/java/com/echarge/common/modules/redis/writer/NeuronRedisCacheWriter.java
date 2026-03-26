/*
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.dao.PessimisticLockingFailureException
 *  org.springframework.data.redis.cache.CacheStatistics
 *  org.springframework.data.redis.cache.CacheStatisticsCollector
 *  org.springframework.data.redis.cache.RedisCacheWriter
 *  org.springframework.data.redis.connection.RedisConnection
 *  org.springframework.data.redis.connection.RedisConnectionFactory
 *  org.springframework.data.redis.connection.RedisStringCommands$SetOption
 *  org.springframework.data.redis.core.Cursor
 *  org.springframework.data.redis.core.ScanOptions
 *  org.springframework.data.redis.core.types.Expiration
 *  org.springframework.lang.Nullable
 *  org.springframework.util.Assert
 */
package com.echarge.common.modules.redis.writer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * @author Edwin
 */
public class NeuronRedisCacheWriter
implements RedisCacheWriter {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(NeuronRedisCacheWriter.class);
    private final RedisConnectionFactory connectionFactory;
    private final Duration sleepTime;
    private final CacheStatisticsCollector statistics = CacheStatisticsCollector.create();

    public NeuronRedisCacheWriter(RedisConnectionFactory connectionFactory) {
        this(connectionFactory, Duration.ZERO);
    }

    public NeuronRedisCacheWriter(RedisConnectionFactory connectionFactory, Duration sleepTime) {
        Assert.notNull(connectionFactory, "ConnectionFactory must not be null!");
        Assert.notNull(sleepTime, "SleepTime must not be null!");
        this.connectionFactory = connectionFactory;
        this.sleepTime = sleepTime;
    }

    public void put(String name, byte[] key, byte[] value, @Nullable Duration ttl) {
        Assert.notNull(name, "Name must not be null!");
        Assert.notNull(key, "Key must not be null!");
        Assert.notNull(value, "Value must not be null!");
        this.execute(name, connection -> {
            if (NeuronRedisCacheWriter.shouldExpireWithin(ttl)) {
                connection.set(key, value, Expiration.from((long)ttl.toMillis(), (TimeUnit)TimeUnit.MILLISECONDS), RedisStringCommands.SetOption.upsert());
            } else {
                connection.set(key, value);
            }
            return "OK";
        });
    }

    public byte[] get(String name, byte[] key) {
        Assert.notNull(name, "Name must not be null!");
        Assert.notNull(key, "Key must not be null!");
        return this.execute(name, connection -> connection.get(key));
    }

    public byte[] putIfAbsent(String name, byte[] key, byte[] value, @Nullable Duration ttl) {
        Assert.notNull(name, "Name must not be null!");
        Assert.notNull(key, "Key must not be null!");
        Assert.notNull(value, "Value must not be null!");
        return this.execute(name, connection -> {
            if (this.isLockingCacheWriter()) {
                this.doLock(name, connection);
            }
            try {
                boolean put = NeuronRedisCacheWriter.shouldExpireWithin(ttl)
                        ? connection.set(key, value, Expiration.from(ttl), RedisStringCommands.SetOption.ifAbsent())
                        : connection.setNX(key, value);
                if (!put) {
                    return connection.get(key);
                }
                return null;
            } finally {
                if (this.isLockingCacheWriter()) {
                    this.doUnlock(name, connection);
                }
            }
        });
    }

    public void remove(String name, byte[] key) {
        Assert.notNull(name, "Name must not be null!");
        Assert.notNull(key, "Key must not be null!");
        String keyString = new String(key);
        log.info("redis remove key:" + keyString);
        String keyIsAll = "*";
        if (keyString != null && keyString.endsWith(keyIsAll)) {
            this.execute(name, connection -> {
                ScanOptions options = ScanOptions.scanOptions().match(keyString).count(100000L).build();
                HashSet<byte[]> keys = new HashSet<>();
                try (Cursor<byte[]> cursor = connection.scan(options)) {
                    while (cursor.hasNext()) {
                        keys.add(cursor.next());
                    }
                }
                int delNum = 0;
                for (byte[] keyByte : keys) {
                    delNum = (int)((long)delNum + connection.del((byte[][])new byte[][]{keyByte}));
                }
                return delNum;
            });
        } else {
            this.execute(name, connection -> connection.del((byte[][])new byte[][]{key}));
        }
    }

    public void clean(String name, byte[] pattern) {
        Assert.notNull(name, "Name must not be null!");
        Assert.notNull(pattern, "Pattern must not be null!");
        this.execute(name, connection -> {
            boolean wasLocked = false;
            try {
                if (this.isLockingCacheWriter()) {
                    this.doLock(name, (RedisConnection)connection);
                    wasLocked = true;
                }
                ScanOptions options = ScanOptions.scanOptions().match(new String(pattern)).count(100000L).build();
                HashSet<byte[]> keys = new HashSet<>();
                try (Cursor<byte[]> cursor = connection.scan(options)) {
                    while (cursor.hasNext()) {
                        keys.add(cursor.next());
                    }
                }
                for (byte[] keyByte : keys) {
                    connection.del((byte[][])new byte[][]{keyByte});
                }
            }
            finally {
                if (wasLocked && this.isLockingCacheWriter()) {
                    this.doUnlock(name, (RedisConnection)connection);
                }
            }
            return "OK";
        });
    }

    void lock(String name) {
        this.execute(name, connection -> this.doLock(name, (RedisConnection)connection));
    }

    void unlock(String name) {
        this.executeLockFree(connection -> this.doUnlock(name, (RedisConnection)connection));
    }

    private Boolean doLock(String name, RedisConnection connection) {
        return connection.set(NeuronRedisCacheWriter.createCacheLockKey(name), new byte[0], Expiration.seconds((long)180L), RedisStringCommands.SetOption.SET_IF_ABSENT);
    }

    private Long doUnlock(String name, RedisConnection connection) {
        return connection.del((byte[][])new byte[][]{NeuronRedisCacheWriter.createCacheLockKey(name)});
    }

    boolean doCheckLock(String name, RedisConnection connection) {
        return connection.exists(NeuronRedisCacheWriter.createCacheLockKey(name));
    }

    private boolean isLockingCacheWriter() {
        return !this.sleepTime.isZero() && !this.sleepTime.isNegative();
    }

    /**
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private <T> T execute(String name, Function<RedisConnection, T> callback) {
        try (RedisConnection connection = this.connectionFactory.getConnection();){
            this.checkAndPotentiallyWaitUntilUnlocked(name, connection);
            T t = callback.apply(connection);
            return t;
        }
    }

    private void executeLockFree(Consumer<RedisConnection> callback) {
        try (RedisConnection connection = this.connectionFactory.getConnection();){
            callback.accept(connection);
        }
    }

    private void checkAndPotentiallyWaitUntilUnlocked(String name, RedisConnection connection) {
        if (this.isLockingCacheWriter()) {
            try {
                while (this.doCheckLock(name, connection)) {
                    Thread.sleep(this.sleepTime.toMillis());
                }
            }
            catch (InterruptedException var4) {
                Thread.currentThread().interrupt();
                throw new PessimisticLockingFailureException(String.format("Interrupted while waiting to unlock cache %s", name), var4);
            }
        }
    }

    private static boolean shouldExpireWithin(@Nullable Duration ttl) {
        return ttl != null && !ttl.isZero() && !ttl.isNegative();
    }

    private static byte[] createCacheLockKey(String name) {
        return (name + "~lock").getBytes(StandardCharsets.UTF_8);
    }

    public CacheStatistics getCacheStatistics(String cacheName) {
        return this.statistics.getCacheStatistics(cacheName);
    }

    public void clearStatistics(String name) {
    }

    public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector cacheStatisticsCollector) {
        return null;
    }

    public CompletableFuture<byte[]> retrieve(String name, byte[] key, Duration ttl) {
        return null;
    }

    public CompletableFuture<Void> store(String name, byte[] key, byte[] value, Duration ttl) {
        return null;
    }
}

