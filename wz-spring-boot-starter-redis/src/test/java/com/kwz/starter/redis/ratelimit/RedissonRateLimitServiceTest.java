package com.kwz.starter.redis.ratelimit;

import com.kwz.common.exception.BizException;
import com.kwz.starter.redis.properties.WzRedisProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedissonRateLimitServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RRateLimiter rateLimiter;

    private WzRedisProperties properties;
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        properties = new WzRedisProperties();
        properties.setKeyPrefix("demo:");
        properties.getRateLimit().setRate(10);
        properties.getRateLimit().setInterval(Duration.ofSeconds(1));
        rateLimitService = new RedissonRateLimitService(redissonClient, properties);
        when(redissonClient.getRateLimiter("demo:rate:api:getUser")).thenReturn(rateLimiter);
    }

    @Test
    void shouldAcquireWhenAllowed() {
        when(rateLimiter.trySetRate(eq(RateType.OVERALL), eq(10L), eq(1L), eq(RateIntervalUnit.SECONDS)))
                .thenReturn(true);
        when(rateLimiter.tryAcquire()).thenReturn(true);
        when(rateLimiter.expire(Duration.ofSeconds(1))).thenReturn(true);

        assertThat(rateLimitService.tryAcquire("api:getUser")).isTrue();
        verify(rateLimiter).tryAcquire();
        verify(rateLimiter).expire(Duration.ofSeconds(1));
    }

    @Test
    void shouldRejectWhenExceeded() {
        when(rateLimiter.trySetRate(eq(RateType.OVERALL), eq(5L), eq(1L), eq(RateIntervalUnit.MINUTES)))
                .thenReturn(true);
        when(rateLimiter.tryAcquire()).thenReturn(false);
        when(rateLimiter.expire(Duration.ofMinutes(1))).thenReturn(true);

        assertThat(rateLimitService.tryAcquire("api:getUser", 5, Duration.ofMinutes(1))).isFalse();
    }

    @Test
    void shouldUseCustomKeyTtl() {
        when(rateLimiter.trySetRate(eq(RateType.OVERALL), eq(5L), eq(10L), eq(RateIntervalUnit.SECONDS)))
                .thenReturn(true);
        when(rateLimiter.tryAcquire()).thenReturn(true);
        when(rateLimiter.expire(Duration.ofMinutes(5))).thenReturn(true);

        rateLimitService.tryAcquire("api:getUser", 5, Duration.ofSeconds(10), Duration.ofMinutes(5));

        verify(rateLimiter).expire(Duration.ofMinutes(5));
    }

    @Test
    void shouldSkipExpirationWhenKeyTtlDisabled() {
        properties.getRateLimit().setKeyTtl(Duration.ZERO);
        when(rateLimiter.trySetRate(eq(RateType.OVERALL), eq(10L), eq(1L), eq(RateIntervalUnit.SECONDS)))
                .thenReturn(true);
        when(rateLimiter.tryAcquire()).thenReturn(true);

        rateLimitService.tryAcquire("api:getUser");

        verify(rateLimiter, never()).expire(org.mockito.ArgumentMatchers.any(Duration.class));
    }

    @Test
    void shouldThrowWhenCheckFailed() {
        when(rateLimiter.trySetRate(eq(RateType.OVERALL), eq(10L), eq(1L), eq(RateIntervalUnit.SECONDS)))
                .thenReturn(true);
        when(rateLimiter.tryAcquire()).thenReturn(false);
        when(rateLimiter.expire(Duration.ofSeconds(1))).thenReturn(true);

        assertThatThrownBy(() -> rateLimitService.check("api:getUser"))
                .isInstanceOf(BizException.class);
    }
}
