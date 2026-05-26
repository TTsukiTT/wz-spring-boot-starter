# wz-common

纯 Java 工具层，**不依赖 Spring**，可被任何模块或业务代码直接引用。

## 依赖

```xml
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-common</artifactId>
</dependency>
```

## 包结构

```
com.kwz.common/
├── result/         # 统一响应
│   └── Result.java
└── exception/      # 异常体系
    ├── ErrorCode.java
    ├── BizException.java
    └── GlobalErrorCode.java
```

## Result — 统一 API 响应体

对外 JSON **仅包含 4 个字段**：`code`、`message`、`data`、`timestamp`。i18n 相关元数据（`messageKey` 等）为内部字段，不会出现在响应中。

```java
public class Result<T> implements Serializable {
    private int code;
    private String message;
    private T data;
    private long timestamp;
}
```

### 用法

```java
// 成功 — 响应写出前自动解析 wz.error.0
return Result.ok(user);

// 自定义成功消息 key
return Result.ok(user, "order.created", orderNo);

// 失败 — 按 ErrorCode 解析
return Result.fail(GlobalErrorCode.NOT_FOUND);

// 原始 message，不解析（如已拼接好的校验明细）
return Result.failRaw(400, "name: 不能为空");
```

响应示例（`message` 由 Advice 按 Locale 解析后输出）：

```json
{
  "code": 0,
  "message": "成功",
  "data": { "id": 1, "name": "张三" },
  "timestamp": 1716633600000
}
```

> 不会出现 `messageKey`、`messageArgs`、`resolveI18n` 等内部字段。

## 异常体系

### 带占位符的业务异常

```java
throw new BizException(OrderErrorCode.ORDER_NOT_FOUND, orderId);
```

消息模板示例（`messages_en.properties`）：

```properties
wz.error.10001=Order {0} not found
```

### ErrorCode 接口

各业务模块实现此接口定义自己的错误码：

```java
public interface ErrorCode {
    int getCode();
    String getMessage();

    /** i18n 消息 key，默认 wz.error.{code} */
    default String getMessageKey() {
        return "wz.error." + getCode();
    }
}
```

### GlobalErrorCode — 全局通用错误码

| 枚举值 | Code | Message |
|--------|------|---------|
| `SUCCESS` | 0 | success |
| `BAD_REQUEST` | 400 | 请求参数错误 |
| `UNAUTHORIZED` | 401 | 未授权 |
| `FORBIDDEN` | 403 | 无权限 |
| `NOT_FOUND` | 404 | 资源不存在 |
| `INTERNAL_ERROR` | 500 | 系统内部错误 |

### BizException — 业务异常

```java
// 使用错误码枚举
throw new BizException(GlobalErrorCode.NOT_FOUND);

// 自定义 code 和 message
throw new BizException(10001, "订单不存在");
```

## 自定义业务错误码

```java
@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(10001, "订单不存在"),
    ORDER_EXPIRED(10002, "订单已过期");

    private final int code;
    private final String message;
}
```

```java
throw new BizException(OrderErrorCode.ORDER_NOT_FOUND);
```
