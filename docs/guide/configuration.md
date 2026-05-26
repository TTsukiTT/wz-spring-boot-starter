# 配置说明

所有配置统一使用 `wz.*` 前缀。

## 全局配置

由 `wz-spring-boot-starter-core` 提供，前缀 `wz`：

```yaml
wz:
  app-name: my-service    # 应用标识，用于日志/链路追踪，默认 app
  debug: false           # 是否开启 debug 模式，默认 false
```

对应配置类：`com.kwz.starter.core.properties.WzProperties`

## 多语言（i18n）

由 `wz-spring-boot-starter-core` + `wz-spring-boot-starter-web` 提供，前缀 `wz.i18n`：

```yaml
wz:
  i18n:
    enabled: true                              # 是否启用，默认 true
    basenames:                                 # 消息资源文件
      - i18n/messages
    default-locale: zh_CN                       # 默认语言
    encoding: UTF-8
    param-name: lang                            # URL 切换语言参数，如 ?lang=en，设为空禁用
    use-code-as-default-message: false        # 找不到 key 时是否返回 key 本身
```

### 语言切换方式

1. **Accept-Language 请求头**（默认）

```http
Accept-Language: en-US
```

2. **URL 参数**（需配置 `param-name`）

```http
GET /api/users/1?lang=en
```

### 业务项目添加自定义消息

在业务项目 `src/main/resources/i18n/` 下创建消息文件：

```
i18n/messages.properties
i18n/messages_zh_CN.properties
i18n/messages_en.properties
```

```properties
# messages_en.properties
wz.error.10001=Order not found
wz.error.10002=Order expired
```

错误码 key 约定：`wz.error.{code}`，也可在 `ErrorCode` 中重写 `getMessageKey()`。

## 模块配置（规划）

以下配置项为各模块规划中的配置，部分模块尚未实现。

### Web

```yaml
wz:
  web:
    wrap-result:
      enabled: true    # Controller 返回值自动包装为 Result，默认 true
```

### MyBatis

```yaml
wz:
  mybatis:
    enabled: true
    logic-delete: true      # 逻辑删除
    audit: true               # 自动填充 createTime/updateTime
```

### Redis

```yaml
wz:
  redis:
    enabled: true
    key-prefix: "myapp:"
```

### Security

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

### OSS

```yaml
wz:
  oss:
    enabled: true
    provider: minio           # minio | aliyun | s3
    endpoint: http://localhost:9000
```

### MQ

```yaml
wz:
  mq:
    enabled: true
    provider: rocketmq        # rocketmq | kafka | rabbitmq
```

### Log

```yaml
wz:
  log:
    file:
      enabled: true              # 文件滚动日志，默认 true
      path: logs                 # 日志目录
      name: app                  # 文件名，默认取 spring.application.name
      max-file-size: 10MB        # 按大小分片
      max-history: 30            # 按日期保留天数
      total-size-cap: 3GB        # 总归档大小上限
      compress: true             # gzip 压缩归档
      clean-history-on-start: false
    console:
      enabled: true              # 控制台输出
    pattern:
      console:                   # 控制台格式，留空=内置美化格式
      file:                      # 文件格式，留空=内置纯文本格式
```

详见 [wz-spring-boot-starter-log](/modules/log)。

## 关闭模块

任意模块均可通过 `enabled: false` 关闭自动配置：

```yaml
wz:
  web:
    enabled: false
```

## 覆盖默认 Bean

业务项目可通过注册同名 Bean 覆盖 Starter 默认实现，无需修改配置：

```java
@Configuration
public class CustomConfig {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new MyExceptionHandler();
    }
}
```

详见 [扩展机制](/development/extend)。
