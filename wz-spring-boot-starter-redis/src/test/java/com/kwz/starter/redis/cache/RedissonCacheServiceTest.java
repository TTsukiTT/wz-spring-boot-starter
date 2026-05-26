package com.kwz.starter.redis.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwz.starter.redis.properties.WzRedisProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedissonCacheServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private RBucket bucket;

    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        WzRedisProperties properties = new WzRedisProperties();
        properties.setKeyPrefix("demo:");
        cacheService = new RedissonCacheService(redissonClient, new ObjectMapper(), properties);
        when(redissonClient.getBucket(eq("demo:user:1"), eq(StringCodec.INSTANCE))).thenReturn(bucket);
    }

    @Test
    void shouldReturnCachedValueWithoutCallingLoader() {
        when(bucket.get()).thenReturn("{\"id\":1,\"name\":\"kwz\"}");

        AtomicInteger loadCount = new AtomicInteger();
        User cached = cacheService.get("user:1", () -> {
            loadCount.incrementAndGet();
            return new User(1L, "db");
        }, User.class);

        assertThat(cached.getId()).isEqualTo(1L);
        assertThat(cached.getName()).isEqualTo("kwz");
        assertThat(loadCount).hasValue(0);
    }

    @Test
    void shouldLoadAndCacheWhenMiss() {
        when(bucket.get()).thenReturn(null);

        User loaded = cacheService.get("user:1", () -> new User(1L, "db"), User.class);

        assertThat(loaded.getName()).isEqualTo("db");
        verify(bucket).set(eq("{\"id\":1,\"name\":\"db\"}"));
    }

    @Test
    void shouldPutWithTtl() {
        cacheService.put("user:1", new User(1L, "db"), Duration.ofMinutes(5));

        verify(bucket).set(eq("{\"id\":1,\"name\":\"db\"}"), eq(Duration.ofMinutes(5)));
    }

    @Test
    void shouldEvictKey() {
        cacheService.evict("user:1");

        verify(bucket).delete();
    }

    @Test
    void shouldNotCacheNullValue() {
        when(bucket.get()).thenReturn(null);

        User result = cacheService.get("user:1", () -> null, User.class);

        assertThat(result).isNull();
        verify(bucket, never()).set(any());
    }

    static class User {

        private Long id;
        private String name;

        User() {
        }

        User(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
