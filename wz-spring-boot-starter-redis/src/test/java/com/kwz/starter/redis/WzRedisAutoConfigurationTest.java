package com.kwz.starter.redis;

import com.kwz.starter.redis.cache.CacheService;
import com.kwz.starter.redis.lock.LockService;
import com.kwz.starter.redis.ratelimit.RateLimitService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(
        classes = WzRedisAutoConfigurationTest.TestApplication.class,
        properties = "spring.autoconfigure.exclude=org.redisson.spring.starter.RedissonAutoConfigurationV2"
)
class WzRedisAutoConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldRegisterCacheAndLockServices() {
        assertThat(applicationContext.getBean(CacheService.class)).isNotNull();
        assertThat(applicationContext.getBean(LockService.class)).isNotNull();
        assertThat(applicationContext.getBean(RateLimitService.class)).isNotNull();
    }

    @SpringBootApplication
    @Import(WzRedisAutoConfigurationTest.RedisTestConfiguration.class)
    static class TestApplication {
    }

    static class RedisTestConfiguration {

        @Bean
        RedissonClient redissonClient() {
            return mock(RedissonClient.class);
        }
    }
}
