package com.kwz.starter.redis.ratelimit;

import com.kwz.starter.redis.properties.WzRedisProperties;
import com.kwz.starter.redis.support.RedisKeyPrefix;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.Duration;

/**
 * 基于 Redisson {@link RRateLimiter} 的分布式限流实现
 */
public class RedissonRateLimitService implements RateLimitService {

    private static final String RATE_LIMIT_NAMESPACE = "rate:";

    private final RedissonClient redissonClient;
    private final WzRedisProperties properties;

    public RedissonRateLimitService(RedissonClient redissonClient, WzRedisProperties properties) {
        this.redissonClient = redissonClient;
        this.properties = properties;
    }

    @Override
    public boolean tryAcquire(String key) {
        WzRedisProperties.RateLimit rateLimit = properties.getRateLimit();
        return tryAcquire(key, rateLimit.getRate(), rateLimit.getInterval(), null);
    }

    @Override
    public boolean tryAcquire(String key, long rate, Duration interval, Duration keyTtl) {
        RRateLimiter limiter = rateLimiter(key);
        initRateIfNeeded(limiter, rate, interval);
        refreshKeyExpiration(limiter, interval, keyTtl);
        return limiter.tryAcquire();
    }

    private RRateLimiter rateLimiter(String key) {
        String fullKey = RedisKeyPrefix.apply(properties.getKeyPrefix(), RATE_LIMIT_NAMESPACE + key);
        return redissonClient.getRateLimiter(fullKey);
    }

    private void initRateIfNeeded(RRateLimiter limiter, long rate, Duration interval) {
        long effectiveRate = Math.max(1, rate);
        RateInterval rateInterval = RateInterval.from(interval);
        limiter.trySetRate(RateType.OVERALL, effectiveRate, rateInterval.value, rateInterval.unit);
    }

    private void refreshKeyExpiration(RRateLimiter limiter, Duration interval, Duration keyTtl) {
        Duration ttl = resolveKeyTtl(interval, keyTtl);
        if (ttl != null) {
            limiter.expire(ttl);
        }
    }

    private Duration resolveKeyTtl(Duration interval, Duration keyTtl) {
        Duration candidate = keyTtl != null ? keyTtl : properties.getRateLimit().getKeyTtl();
        if (candidate != null) {
            return isDisabled(candidate) ? null : candidate;
        }
        if (interval == null || isDisabled(interval)) {
            return Duration.ofSeconds(1);
        }
        return interval;
    }

    private boolean isDisabled(Duration duration) {
        return duration.isZero() || duration.isNegative();
    }

    private record RateInterval(long value, RateIntervalUnit unit) {

        private static RateInterval from(Duration interval) {
            if (interval == null || interval.isZero() || interval.isNegative()) {
                return new RateInterval(1, RateIntervalUnit.SECONDS);
            }
            long millis = interval.toMillis();
            if (millis < 1000) {
                return new RateInterval(Math.max(1, millis), RateIntervalUnit.MILLISECONDS);
            }
            if (millis % 60_000 == 0) {
                return new RateInterval(millis / 60_000, RateIntervalUnit.MINUTES);
            }
            if (millis % 1000 == 0) {
                return new RateInterval(millis / 1000, RateIntervalUnit.SECONDS);
            }
            return new RateInterval(Math.max(1, millis), RateIntervalUnit.MILLISECONDS);
        }
    }
}
