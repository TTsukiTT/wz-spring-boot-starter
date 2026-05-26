# 扩展机制

WZ Starter 提供多种扩展方式，业务项目可在不修改 Starter 源码的情况下定制行为。

## 1. 覆盖默认 Bean

所有 Starter 默认 Bean 均使用 `@ConditionalOnMissingBean` 注册。业务项目注册同名 Bean 即可覆盖：

```java
@Configuration
public class CustomWebConfig {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new MyGlobalExceptionHandler();
    }
}
```

`MyGlobalExceptionHandler` 可继承 `GlobalExceptionHandler` 并扩展：

```java
public class MyGlobalExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public Result<Void> handleCustom(CustomException e) {
        return Result.fail(e.getCode(), e.getMessageKey(), e.getMessage());
    }
}
```

## 2. 自定义错误码

实现 `ErrorCode` 接口定义模块级错误码：

```java
@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(10001, "订单不存在"),
    ORDER_EXPIRED(10002, "订单已过期"),
    INSUFFICIENT_STOCK(10003, "库存不足");

    private final int code;
    private final String message;
}
```

使用：

```java
throw new BizException(OrderErrorCode.ORDER_NOT_FOUND);
```

建议错误码分段规划：

| 范围 | 模块 |
|------|------|
| 0 | 成功 |
| 400-599 | HTTP 标准错误（GlobalErrorCode） |
| 10000-19999 | 订单模块 |
| 20000-29999 | 用户模块 |
| 30000-39999 | 支付模块 |

## 3. SPI 扩展（规划中）

部分模块将提供 SPI 接口，支持多种实现：

```java
// OSS 模块
public interface FileStorage {
    String upload(InputStream input, String filename, String contentType);
    InputStream download(String path);
    void delete(String path);
}

// 业务项目实现
@Component
@ConditionalOnProperty(prefix = "wz.oss", name = "provider", havingValue = "custom")
public class CustomFileStorage implements FileStorage {
    // ...
}
```

## 4. 关闭模块

通过配置关闭不需要的 Starter 自动配置：

```yaml
wz:
  web:
    enabled: false
```

## 5. 自定义配置属性

业务项目可扩展 `WzProperties` 或定义独立的 `@ConfigurationProperties`：

```java
@Data
@ConfigurationProperties(prefix = "myapp")
public class MyAppProperties {
    private String apiKey;
    private Duration timeout = Duration.ofSeconds(30);
}
```

```java
@Configuration
@EnableConfigurationProperties(MyAppProperties.class)
public class MyAppConfig {
    // ...
}
```

## 扩展原则

- **不修改 Starter 源码** — 所有定制通过 Bean 覆盖、SPI 实现、配置开关完成
- **最小依赖** — 只引入需要的 Starter 模块
- **向后兼容** — Starter 升级不应破坏已有的扩展实现
