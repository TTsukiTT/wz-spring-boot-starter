package com.kwz.starter.redis.autoconfigure;

import com.kwz.starter.redis.properties.WzRedisProperties;
import com.kwz.starter.redis.ratelimit.RateLimitAspect;
import com.kwz.starter.redis.ratelimit.RateLimitService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@AutoConfiguration(after = WzRedisAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
@ConditionalOnProperty(prefix = "wz.redis.rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableAspectJAutoProxy
public class WzRedisRateLimitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RateLimitAspect rateLimitAspect(RateLimitService rateLimitService, WzRedisProperties properties) {
        return new RateLimitAspect(rateLimitService, properties);
    }
}
