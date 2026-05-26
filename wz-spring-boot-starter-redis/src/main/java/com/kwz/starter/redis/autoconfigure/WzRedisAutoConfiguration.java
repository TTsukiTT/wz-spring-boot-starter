package com.kwz.starter.redis.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwz.starter.redis.cache.CacheService;
import com.kwz.starter.redis.cache.RedissonCacheService;
import com.kwz.starter.redis.lock.LockService;
import com.kwz.starter.redis.lock.RedissonLockService;
import com.kwz.starter.redis.properties.WzRedisProperties;
import com.kwz.starter.redis.ratelimit.RateLimitService;
import com.kwz.starter.redis.ratelimit.RedissonRateLimitService;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = RedissonAutoConfigurationV2.class)
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnProperty(prefix = "wz.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WzRedisProperties.class)
public class WzRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheService cacheService(RedissonClient redissonClient,
                                     ObjectProvider<ObjectMapper> objectMapperProvider,
                                     WzRedisProperties properties) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        return new RedissonCacheService(redissonClient, objectMapper, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public LockService lockService(RedissonClient redissonClient, WzRedisProperties properties) {
        return new RedissonLockService(redissonClient, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitService rateLimitService(RedissonClient redissonClient, WzRedisProperties properties) {
        return new RedissonRateLimitService(redissonClient, properties);
    }
}
