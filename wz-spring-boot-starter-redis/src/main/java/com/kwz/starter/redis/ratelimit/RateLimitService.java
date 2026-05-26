package com.kwz.starter.redis.ratelimit;

import com.kwz.common.exception.BizException;
import com.kwz.starter.redis.exception.RedisErrorCode;

import java.time.Duration;

/**
 * 分布式限流接口
 */
public interface RateLimitService {

    /** 使用全局默认速率，尝试获取 1 个许可 */
    boolean tryAcquire(String key);

    /** 指定窗口内最多 {@code rate} 次，尝试获取 1 个许可 */
    default boolean tryAcquire(String key, long rate, Duration interval) {
        return tryAcquire(key, rate, interval, null);
    }

    /**
     * 指定窗口与 key 过期时间，尝试获取 1 个许可。
     *
     * @param keyTtl 限流 key 过期时间，{@code null} 使用全局配置；{@code ZERO} 表示不过期
     */
    boolean tryAcquire(String key, long rate, Duration interval, Duration keyTtl);

    /** 获取失败则抛出 {@link BizException} */
    default void check(String key) {
        if (!tryAcquire(key)) {
            throw new BizException(RedisErrorCode.RATE_LIMIT_EXCEEDED);
        }
    }

    default void check(String key, long rate, Duration interval) {
        check(key, rate, interval, null);
    }

    default void check(String key, long rate, Duration interval, Duration keyTtl) {
        if (!tryAcquire(key, rate, interval, keyTtl)) {
            throw new BizException(RedisErrorCode.RATE_LIMIT_EXCEEDED);
        }
    }
}
