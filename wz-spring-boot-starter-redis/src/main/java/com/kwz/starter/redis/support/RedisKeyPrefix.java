package com.kwz.starter.redis.support;

import org.springframework.util.StringUtils;

/**
 * Redis Key 前缀工具
 */
public final class RedisKeyPrefix {

    private RedisKeyPrefix() {
    }

    public static String apply(String keyPrefix, String key) {
        if (!StringUtils.hasText(keyPrefix)) {
            return key;
        }
        return keyPrefix + key;
    }
}
