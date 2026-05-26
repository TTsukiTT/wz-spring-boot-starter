# wz-spring-boot-starter-redis

Redis 集成 Starter，基于 Redisson 封装缓存抽象、分布式锁与限流。

## 依赖

```xml
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-redis</artifactId>
</dependency>
```

底层依赖：`redisson-spring-boot-starter` 3.41.0

## 能力

| 能力 | 说明 |
|------|------|
| CacheService | 统一缓存读写接口（Cache-Aside + JSON 序列化） |
| LockService | 分布式锁（基于 Redisson） |
| RateLimitService | 分布式限流（基于 Redisson RRateLimiter） |
| @RateLimit | 方法级限流注解，支持 SpEL 动态 key |
| Key 前缀 | 全局 `key-prefix`，避免多应用冲突 |

## 配置

```yaml
wz:
  redis:
    enabled: true
    key-prefix: "myapp:"
    default-ttl: 30m
    lock:
      wait-time: 3s
      lease-time: 30s
    rate-limit:
      enabled: true
      rate: 100              # 默认：1 秒内最多 100 次
      interval: 1s
      key-ttl: 10m           # 限流 key 过期时间；留空默认等于 interval；0s 表示不过期

spring:
  data:
    redis:
      host: localhost
      port: 6379
```

连接参数走 Spring Boot 原生 `spring.data.redis.*`，由 `redisson-spring-boot-starter` 自动创建 `RedissonClient`。

关闭模块：`wz.redis.enabled=false`  
关闭注解限流：`wz.redis.rate-limit.enabled=false`

## 用法

### 编程式限流

```java
@Service
@RequiredArgsConstructor
public class SmsService {

    private final RateLimitService rateLimitService;

    public void send(String phone) {
        rateLimitService.check("sms:" + phone, 1, Duration.ofMinutes(1), Duration.ofMinutes(5));
        // 发送短信
    }
}
```

### 注解限流

```java
@RestController
public class UserController {

    @RateLimit(key = "'user:' + #id", rate = 10, intervalSeconds = 60, keyTtlSeconds = 300)
    @GetMapping("/users/{id}")
    public UserVO getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }
}
```

- `key` 留空时，默认使用 `类名:方法名`
- `rate` / `intervalSeconds` 留空或 ≤0 时，使用 `wz.redis.rate-limit` 全局默认值
- 超限抛出 `BizException`，错误码 `429`

## 抽象接口

```java
public interface CacheService {
    <T> T get(String key, Supplier<T> loader, Class<T> type);
    void put(String key, Object value, Duration ttl);
    void evict(String key);
}

public interface LockService {
    void executeWithLock(String key, Runnable action);
    <T> T executeWithLock(String key, Supplier<T> action);
}

public interface RateLimitService {
    boolean tryAcquire(String key);
    boolean tryAcquire(String key, long rate, Duration interval);
    boolean tryAcquire(String key, long rate, Duration interval, Duration keyTtl);
    void check(String key);
    void check(String key, long rate, Duration interval);
    void check(String key, long rate, Duration interval, Duration keyTtl);
}
```

## 扩展

- 自定义实现：注册同名 Bean 即可覆盖（`@ConditionalOnMissingBean`）
- 获取锁失败：错误码 `423`
- 限流触发：错误码 `429`
