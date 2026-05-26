package com.kwz.starter.redis.lock;

import com.kwz.common.exception.BizException;
import com.kwz.starter.redis.properties.WzRedisProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedissonLockServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    private LockService lockService;

    @BeforeEach
    void setUp() {
        WzRedisProperties properties = new WzRedisProperties();
        properties.setKeyPrefix("demo:");
        properties.getLock().setWaitTime(Duration.ofSeconds(1));
        properties.getLock().setLeaseTime(Duration.ofSeconds(10));
        lockService = new RedissonLockService(redissonClient, properties);
        when(redissonClient.getLock("demo:balance:1")).thenReturn(lock);
    }

    @Test
    void shouldExecuteActionWhenLockAcquired() throws Exception {
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        AtomicBoolean executed = new AtomicBoolean();
        lockService.executeWithLock("balance:1", () -> executed.set(true));

        assertThat(executed).isTrue();
        verify(lock).unlock();
    }

    @Test
    void shouldThrowWhenLockNotAcquired() throws Exception {
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        assertThatThrownBy(() -> lockService.executeWithLock("balance:1", () -> {
        })).isInstanceOf(BizException.class);
    }
}
