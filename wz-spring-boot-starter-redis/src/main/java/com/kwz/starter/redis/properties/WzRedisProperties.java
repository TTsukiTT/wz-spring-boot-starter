package com.kwz.starter.redis.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Redis 配置，前缀 wz.redis.*
 */
@Data
@ConfigurationProperties(prefix = "wz.redis")
public class WzRedisProperties {

    /** 是否启用 Redis 模块 */
    private boolean enabled = true;

    /** 全局 key 前缀，避免多应用冲突 */
    private String keyPrefix = "";

    /** 默认缓存过期时间，留空表示永不过期 */
    private Duration defaultTtl;

    private Lock lock = new Lock();
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class RateLimit {

        /** 是否启用 @RateLimit 注解切面 */
        private boolean enabled = true;

        /** 默认窗口内最大请求数 */
        private long rate = 100;

        /** 默认时间窗口 */
        private Duration interval = Duration.ofSeconds(1);

        /**
         * 限流 key 过期时间；留空则默认等于 {@link #interval}。
         * 设为 {@code 0s} 表示不过期。
         */
        private Duration keyTtl;
    }

    @Data
    public static class Lock {

        /** 获取锁最大等待时间 */
        private Duration waitTime = Duration.ofSeconds(3);

        /** 锁自动释放时间（看门狗未启用时生效） */
        private Duration leaseTime = Duration.ofSeconds(30);
    }
}
