# wz-spring-boot-starter-job

定时任务 Starter，封装分布式任务调度能力。

::: warning 开发中
该模块目前仅有 POM 骨架，功能尚未实现。
:::

## 依赖

```xml
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-job</artifactId>
</dependency>
```

## 规划能力

| 能力 | 说明 |
|------|------|
| `@WzJob` 注解 | 声明式任务注册 |
| XXL-Job 集成 | 分布式任务调度 |
| Spring Schedule | 本地定时任务支持 |

## 规划用法

```java
@Component
public class OrderJobHandler {

    @WzJob("cancelExpiredOrders")
    public void cancelExpiredOrders() {
        orderService.cancelExpired();
    }
}
```

## 规划配置

```yaml
wz:
  job:
    enabled: true
    admin-address: http://xxl-job-admin:8080/xxl-job-admin
    app-name: order-service
    port: 9999
```
