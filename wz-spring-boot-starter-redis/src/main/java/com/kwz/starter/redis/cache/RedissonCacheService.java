package com.kwz.starter.redis.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwz.common.exception.BizException;
import com.kwz.starter.redis.exception.RedisErrorCode;
import com.kwz.starter.redis.properties.WzRedisProperties;
import com.kwz.starter.redis.support.RedisKeyPrefix;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 基于 Redisson 的 JSON 缓存实现
 */
public class RedissonCacheService implements CacheService {

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final WzRedisProperties properties;

    public RedissonCacheService(RedissonClient redissonClient, ObjectMapper objectMapper,
                                WzRedisProperties properties) {
        this.redissonClient = redissonClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public <T> T get(String key, Supplier<T> loader, Class<T> type) {
        String fullKey = RedisKeyPrefix.apply(properties.getKeyPrefix(), key);
        RBucket<String> bucket = bucket(fullKey);
        String json = bucket.get();
        if (StringUtils.hasText(json)) {
            return deserialize(json, type);
        }
        T value = loader.get();
        if (value != null) {
            put(key, value, properties.getDefaultTtl());
        }
        return value;
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        String fullKey = RedisKeyPrefix.apply(properties.getKeyPrefix(), key);
        RBucket<String> bucket = bucket(fullKey);
        String json = serialize(value);
        Duration effectiveTtl = ttl != null ? ttl : properties.getDefaultTtl();
        if (effectiveTtl != null && !effectiveTtl.isZero() && !effectiveTtl.isNegative()) {
            bucket.set(json, effectiveTtl);
        } else {
            bucket.set(json);
        }
    }

    @Override
    public void evict(String key) {
        bucket(RedisKeyPrefix.apply(properties.getKeyPrefix(), key)).delete();
    }

    private RBucket<String> bucket(String fullKey) {
        return redissonClient.getBucket(fullKey, StringCodec.INSTANCE);
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BizException(RedisErrorCode.CACHE_SERIALIZE_FAILED, ex);
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new BizException(RedisErrorCode.CACHE_DESERIALIZE_FAILED, ex);
        }
    }
}
