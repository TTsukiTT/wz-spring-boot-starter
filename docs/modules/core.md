# wz-spring-boot-starter-core

核心自动配置模块，提供全局 `wz.*` 属性绑定与基础 Spring 集成。

## 依赖

```xml
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-core</artifactId>
</dependency>
```

> 通常不需要单独引入，`wz-spring-boot-starter-web` 等上层模块已传递依赖 core。

## 自动配置

自动配置类：`com.kwz.starter.core.autoconfigure.WzCoreAutoConfiguration`

注册文件：

```
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

## WzProperties

全局配置属性，前缀 `wz`：

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `app-name` | String | `app` | 应用标识，用于日志/链路追踪 |
| `debug` | boolean | `false` | 是否开启 debug 模式 |

### 配置示例

```yaml
wz:
  app-name: order-service
  debug: true
```

### 在代码中使用

```java
@Service
@RequiredArgsConstructor
public class MyService {

    private final WzProperties wzProperties;

    public void doSomething() {
        String appName = wzProperties.getAppName();
    }
}
```

## 模块职责

| 能力 | 状态 |
|------|------|
| 全局属性绑定 | ✅ 已实现 |
| 多语言 MessageSource | ✅ 已实现 |
| I18nMessageResolver | ✅ 已实现 |
| Jackson 序列化配置 | 🚧 规划中 |
| 环境标识（dev/test/prod） | 🚧 规划中 |

## 多语言（i18n）

自动配置类：`com.kwz.starter.core.autoconfigure.WzI18nAutoConfiguration`

### I18nMessageResolver

在业务代码中注入使用：

```java
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final I18nMessageResolver i18nMessageResolver;

    public String getWelcomeMessage(String username) {
        return i18nMessageResolver.resolve("welcome.message", username);
    }
}
```

### 内置消息资源

Starter 内置以下资源文件：

```
i18n/messages.properties          # 默认（英文）
i18n/messages_zh_CN.properties  # 简体中文
i18n/messages_en.properties     # 英文
```

包含全局错误码（`wz.error.*`）与 JSR-380 校验消息（`jakarta.validation.constraints.*`）。
