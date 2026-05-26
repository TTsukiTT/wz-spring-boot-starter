package com.kwz.starter.redis.lock;

import java.util.function.Supplier;

/**
 * 分布式锁接口
 */
public interface LockService {

    void executeWithLock(String key, Runnable action);

    <T> T executeWithLock(String key, Supplier<T> action);
}
