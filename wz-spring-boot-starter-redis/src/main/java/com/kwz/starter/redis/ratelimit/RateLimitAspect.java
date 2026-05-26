package com.kwz.starter.redis.ratelimit;

import com.kwz.common.exception.BizException;
import com.kwz.starter.redis.exception.RedisErrorCode;
import com.kwz.starter.redis.properties.WzRedisProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * {@link RateLimit} 注解切面
 */
@Aspect
public class RateLimitAspect {

    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private final RateLimitService rateLimitService;
    private final WzRedisProperties properties;

    public RateLimitAspect(RateLimitService rateLimitService, WzRedisProperties properties) {
        this.rateLimitService = rateLimitService;
        this.properties = properties;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = resolveKey(joinPoint, rateLimit);
        long rate = rateLimit.rate() > 0 ? rateLimit.rate() : properties.getRateLimit().getRate();
        Duration interval = rateLimit.intervalSeconds() > 0
                ? Duration.ofSeconds(rateLimit.intervalSeconds())
                : properties.getRateLimit().getInterval();
        Duration keyTtl = rateLimit.keyTtlSeconds() > 0
                ? Duration.ofSeconds(rateLimit.keyTtlSeconds())
                : null;
        if (!rateLimitService.tryAcquire(key, rate, interval, keyTtl)) {
            throw new BizException(RedisErrorCode.RATE_LIMIT_EXCEEDED);
        }
        return joinPoint.proceed();
    }

    private String resolveKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        if (!StringUtils.hasText(rateLimit.key())) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return signature.getDeclaringType().getSimpleName() + ":" + signature.getName();
        }
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Object[] args = joinPoint.getArgs();
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                target, method, args, PARAMETER_NAME_DISCOVERER);
        String resolved = EXPRESSION_PARSER.parseExpression(rateLimit.key()).getValue(context, String.class);
        if (!StringUtils.hasText(resolved)) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return signature.getDeclaringType().getSimpleName() + ":" + signature.getName();
        }
        return resolved;
    }
}
