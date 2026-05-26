# wz-spring-boot-starter-security

Security 集成 Starter，提供 JWT 认证与 RBAC 权限控制。

::: warning 开发中
该模块目前仅有 POM 骨架，功能尚未实现。
:::

## 依赖

```xml
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-security</artifactId>
</dependency>
```

传递依赖：`wz-spring-boot-starter-web`、`spring-boot-starter-security`

## 规划能力

| 能力 | 说明 |
|------|------|
| JWT 认证 | Token 生成、验证、刷新 |
| 权限注解 | `@RequirePermission`、`@RequireRole` |
| 白名单 | 可配置免认证路径 |
| SecurityContext | 获取当前登录用户信息 |

## 规划配置

```yaml
wz:
  security:
    enabled: true
    jwt:
      secret: your-secret-key
      expire: 7200
    whitelist:
      - /api/auth/**
      - /actuator/**
```

## 规划用法

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @RequirePermission("user:delete")
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok(null);
    }
}
```

```java
@Service
public class OrderService {

    public void createOrder(CreateOrderRequest request) {
        Long userId = SecurityContext.getCurrentUserId();
        // ...
    }
}
```
