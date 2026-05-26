package com.kwz.starter.redis.cache;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 统一缓存读写接口
 */
public interface CacheService {

    /**
     * Cache-Aside：命中缓存直接返回，否则执行 loader 并写入缓存
     */
    <T> T get(String key, Supplier<T> loader, Class<T> type);

    void put(String key, Object value, Duration ttl);

    void evict(String key);
}
