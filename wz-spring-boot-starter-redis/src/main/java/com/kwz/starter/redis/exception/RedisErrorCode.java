package com.kwz.starter.redis.exception;

import com.kwz.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Redis 模块错误码
 */
@Getter
@RequiredArgsConstructor
public enum RedisErrorCode implements ErrorCode {

    LOCK_FAILED(423, "获取分布式锁失败"),
    RATE_LIMIT_EXCEEDED(429, "请求过于频繁"),
    CACHE_SERIALIZE_FAILED(5001, "缓存序列化失败"),
    CACHE_DESERIALIZE_FAILED(5002, "缓存反序列化失败");

    private final int code;
    private final String message;
}
