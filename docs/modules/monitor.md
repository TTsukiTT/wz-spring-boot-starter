# wz-spring-boot-starter-monitor

监控 Starter，集成 Spring Boot Actuator 与健康检查。

::: warning 开发中
该模块目前仅有 POM 骨架，功能尚未实现。
:::

## 依赖

```xml
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-monitor</artifactId>
</dependency>
```

底层依赖：`spring-boot-starter-actuator`

## 规划能力

| 能力 | 说明 |
|------|------|
| 健康检查 | 自定义 HealthIndicator |
| Metrics | Prometheus 指标暴露 |
| 端点安全 | 生产环境端点访问控制 |

## 规划配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
```

## 规划端点

| 端点 | 路径 | 说明 |
|------|------|------|
| Health | `/actuator/health` | 应用健康状态 |
| Info | `/actuator/info` | 应用基本信息 |
| Metrics | `/actuator/metrics` | 运行时指标 |
| Prometheus | `/actuator/prometheus` | Prometheus 格式指标 |

## 规划自定义健康检查

```java
@Component
public class RedisHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // 检查 Redis 连接
        return Health.up().withDetail("redis", "connected").build();
    }
}
```
