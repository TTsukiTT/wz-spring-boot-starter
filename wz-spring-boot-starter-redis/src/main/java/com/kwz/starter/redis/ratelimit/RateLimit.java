package com.kwz.starter.redis.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级分布式限流注解，支持 SpEL 动态 key。
 * <p>
 * 示例：{@code @RateLimit(key = "'user:' + #id", rate = 10, intervalSeconds = 60)}
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /** 限流 key，支持 SpEL；留空则使用方法签名 */
    String key() default "";

    /** 窗口内最大请求数，≤0 时使用 wz.redis.rate-limit.rate */
    long rate() default -1;

    /** 时间窗口（秒），≤0 时使用 wz.redis.rate-limit.interval */
    long intervalSeconds() default -1;

    /** 限流 key 过期时间（秒），≤0 时使用 wz.redis.rate-limit.key-ttl */
    long keyTtlSeconds() default -1;
}
