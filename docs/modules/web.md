# wz-spring-boot-starter-web

Web 层 Starter，提供统一响应、全局异常处理等 Web 横切能力。

## 依赖

```xml
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-web</artifactId>
</dependency>
```

传递依赖：

- `wz-spring-boot-starter-core`
- `wz-common`
- `spring-boot-starter-web`
- `spring-boot-starter-validation`

## 自动配置

自动配置类：`com.kwz.starter.web.autoconfigure.WzWebAutoConfiguration`

激活条件：

- `@ConditionalOnWebApplication(type = SERVLET)` — 仅在 Servlet Web 环境生效

## ResultWrapperResponseBodyAdvice

自动将 Controller 返回值包装为 `Result`，默认开启（`wz.web.wrap-result.enabled=true`）。

| 返回类型 | 处理方式 |
|----------|----------|
| 业务对象 / 集合 / 基本类型 / `String` | 包装为 `Result.ok(data)` |
| `Result` | 不重复包装 |
| `ResponseEntity` | 跳过 |
| `byte[]` / `Resource` / `InputStream` | 跳过（文件流） |
| `@NoWrapResult` 标注的类或方法 | 跳过 |

`String` 返回类型会先包装为 `Result<String>`，再由 `ResultStringResponseBodyAdvice` 序列化为 JSON 字符串（Content-Type: `application/json`）。

## GlobalExceptionHandler

全局异常处理器，自动捕获并转换为 `Result` 响应。消息的多语言解析由 `ResultI18nResponseBodyAdvice` 在响应写出前统一完成。

| 异常类型 | 处理方式 |
|----------|----------|
| `BizException` | 设置 messageKey，由 Advice 解析 |
| `MethodArgumentNotValidException` / `BindException` | `failRaw` 保留校验明细（校验消息本身已 i18n） |
| `Exception` | 返回 `Result.fail(INTERNAL_ERROR)`，Advice 解析 |

### 多语言响应示例

```java
throw new BizException(GlobalErrorCode.NOT_FOUND);
```

请求头 `Accept-Language: en` 时：

```json
{
  "code": 404,
  "message": "Resource not found",
  "data": null,
  "timestamp": 1716633600000
}
```

默认中文（`Accept-Language: zh-CN` 或未指定）：

```json
{
  "code": 404,
  "message": "资源不存在",
  "data": null,
  "timestamp": 1716633600000
}
```

### 未捕获异常响应

```json
{
  "code": 500,
  "message": "系统内部错误",
  "data": null,
  "timestamp": 1716633600000
}
```

## 完整示例

```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    public Result<OrderVO> getOrder(@PathVariable Long id) {
        return Result.ok(orderService.getById(id));
    }

    @PostMapping
    public Result<Long> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return Result.ok(orderService.create(request));
    }
}
```

## ResultI18nResponseBodyAdvice

拦截所有 `Result` 响应，在写出前按当前 Locale 解析 `message`：

- 内部按 `messageKey` 或 `wz.error.{code}` 解析，**解析结果只写入 `message` 字段**
- `messageKey`、`messageArgs`、`resolveI18n` 为内部元数据，**不会出现在 JSON 响应中**
- `resolveI18n = false` 时跳过解析（`Result.failRaw`）

```java
// Accept-Language: en
return Result.ok(user);
// → { "code": 0, "message": "Success", ... }

// Accept-Language: zh-CN
return Result.ok(user);
// → { "code": 0, "message": "成功", ... }
```

## 覆盖默认异常处理器

```java
@Configuration
public class CustomWebConfig {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new MyGlobalExceptionHandler();
    }
}
```

## 模块职责（规划）

| 能力 | 状态 |
|------|------|
| 全局异常处理 | ✅ 已实现 |
| Result 自动包装 | ✅ 已实现 |
| Result 响应多语言 | ✅ 已实现 |
| 多语言 Locale 解析 | ✅ 已实现 |
| 参数校验国际化 | ✅ 已实现 |
| CORS 跨域配置 | 🚧 规划中 |
| 请求日志拦截 | 🚧 规划中 |
| API 文档集成（SpringDoc） | 🚧 规划中 |
