package com.kwz.starter.redis.lock;

import com.kwz.common.exception.BizException;
import com.kwz.starter.redis.exception.RedisErrorCode;
import com.kwz.starter.redis.properties.WzRedisProperties;
import com.kwz.starter.redis.support.RedisKeyPrefix;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 基于 Redisson 的分布式锁实现
 */
public class RedissonLockService implements LockService {

    private final RedissonClient redissonClient;
    private final WzRedisProperties properties;

    public RedissonLockService(RedissonClient redissonClient, WzRedisProperties properties) {
        this.redissonClient = redissonClient;
        this.properties = properties;
    }

    @Override
    public void executeWithLock(String key, Runnable action) {
        executeWithLock(key, () -> {
            action.run();
            return null;
        });
    }

    @Override
    public <T> T executeWithLock(String key, Supplier<T> action) {
        RLock lock = redissonClient.getLock(RedisKeyPrefix.apply(properties.getKeyPrefix(), key));
        boolean acquired = false;
        try {
            acquired = lock.tryLock(
                    properties.getLock().getWaitTime().toMillis(),
                    properties.getLock().getLeaseTime().toMillis(),
                    TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new BizException(RedisErrorCode.LOCK_FAILED);
            }
            return action.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BizException(RedisErrorCode.LOCK_FAILED);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
