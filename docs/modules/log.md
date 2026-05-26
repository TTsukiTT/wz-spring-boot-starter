# wz-spring-boot-starter-log

生产级文件日志 Starter，基于 Logback 实现按日期 + 按大小滚动、归档压缩。

## 依赖

```xml
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-log</artifactId>
</dependency>
```

## 能力

| 能力 | 说明 |
|------|------|
| 按日期分文件 | 每天一个归档序列，文件名含 `yyyy-MM-dd` |
| 按大小分文件 | 单文件超过阈值后同日内按 `%i` 序号分片 |
| 日志压缩 | 归档文件默认 `.log.gz` gzip 压缩 |
| 容量控制 | 保留天数 + 总归档大小上限 |
| 控制台输出 | 可独立开关，生产环境可关闭 |
| TraceId | 基于 TLog，Spring Boot 3 Jakarta Filter 适配 |

## 配置

```yaml
spring:
  application:
    name: order-service

wz:
  log:
    trace:
      enabled: true                # 启用 TLog 链路追踪，默认 true
      mdc-key: traceId             # MDC 键名，对应 %X{traceId}
    file:
      enabled: true              # 启用文件日志，默认 true
      path: logs                 # 日志目录
      name: order-service        # 未配置时自动取 spring.application.name
      max-file-size: 10MB        # 单文件大小上限
      max-history: 30            # 保留 30 天
      total-size-cap: 3GB        # 总归档大小上限
      compress: true             # gzip 压缩归档
      clean-history-on-start: false
    console:
      enabled: true              # false = 关闭控制台，仅写文件
    pattern:
      console:                   # 留空使用内置美化格式（彩色级别 + TraceId）
      file:                      # 留空使用内置格式（纯文本，便于采集）
```

### 内置日志格式

**控制台**（彩色，WARN 为黄色，含 TraceId）：

```
2026-05-25 17:41:41.862 ERROR [nio-8080-exec-1] [11794076298070144]  c.k.s.d.a.controller.UserController  : getUser id: 1
2026-05-25 14:30:02.456 WARN  [nio-8080-exec-1] [11794076298070144]  com.kwz.demo.UserController : 参数异常
2026-05-25 14:30:01.123 INFO  [nio-8080-exec-1] [11794076298070144]  com.kwz.demo.UserController : 查询用户成功
```

**文件**（纯文本）：

```
2026-05-25 17:41:41.862 ERROR [http-nio-8080-exec-1] [11794076298070144] com.kwz.demo.UserController : getUser id: 1
```

HTTP 响应头会返回 `tlogTraceId`，便于前端或网关获取整条链路 ID。

### TraceId（TLog）

引入 `wz-spring-boot-starter-log` 后默认启用。TLog 官方 Starter 基于 `javax.servlet`，在 **Spring Boot 3** 下不生效；本 Starter 参考 [SpringBoot3 适配 TLog](https://juejin.cn/post/7408202391504371727)，通过 **Jakarta Servlet Filter + MVC 拦截器** 适配，并将 TraceId 写入 MDC（`%X{traceId}`）。

跨服务传递时在请求头携带：

```
tlogTraceId: 11794076298070144
```

关闭 TraceId：

```yaml
wz:
  log:
    trace:
      enabled: false
```

自定义格式示例：

```yaml
wz:
  log:
    pattern:
      console: "%clr(%d{HH:mm:ss.SSS}){faint} %clr(%-5level) %clr(%logger{36}){cyan} : %msg%n"
      file: "%d{yyyy-MM-dd HH:mm:ss} [%level] %logger - %msg%n"
```

## 日志文件示例

```
logs/
├── order-service.log                        # 当前写入文件
├── order-service.log.2026-05-24.0.gz         # 当天第一片（Spring Boot 默认命名）
├── order-service.log.2026-05-24.1.gz         # 同一天超过 10MB 后的第二片
└── order-service.log.2026-05-25.0.gz
```

## 实现原理

Starter 通过 `WzLogEnvironmentPostProcessor` 在 Logback 初始化前，将 `wz.log.*` 映射为 Spring Boot 原生配置：

| wz.log.* | Spring Boot 等价配置 |
|----------|---------------------|
| `file.path` + `file.name` | `logging.file.name` |
| `file.max-file-size` | `logging.logback.rollingpolicy.max-file-size` |
| `file.max-history` | `logging.logback.rollingpolicy.max-history` |
| `file.total-size-cap` | `logging.logback.rollingpolicy.total-size-cap` |
| `file.compress` | `logging.logback.rollingpolicy.file-name-pattern`（`.gz`） |
| `console.enabled: false` | `logging.config=classpath:wz-logback-file-only.xml`（Spring Boot 3.4 无原生开关） |
| `trace.enabled` | 启用 `WzTLogServletFilter`（Jakarta 适配） |

## 滚动策略

Spring Boot 内置 `SizeAndTimeBasedRollingPolicy`：

- **按日期**：`%d{yyyy-MM-dd}` 每天切换归档序列
- **按大小**：`%i` 在同一天内文件超过 `max-file-size` 时递增
- **压缩**：`compress: true` 时归档为 `.log.gz`

## 生产环境建议

```yaml
# application-prod.yml
wz:
  log:
    file:
      enabled: true
      path: /var/log/apps
      max-file-size: 50MB
      max-history: 90
      total-size-cap: 20GB
      compress: true
    console:
      enabled: false             # 生产环境关闭控制台，仅写文件

logging:
  level:
    root: INFO
    com.kwz: INFO
```

## 自定义 Logback

若需完全自定义，可关闭 Starter 文件日志并自行配置：

```yaml
wz:
  log:
    file:
      enabled: false

logging:
  config: classpath:logback-spring.xml
```

## 排查：未生成日志文件

1. **确认依赖已引入并重新安装 Starter**

```bash
# 在 starter 项目根目录
mvn clean install -DskipTests
```

业务项目 `pom.xml` 需显式引入 `wz-spring-boot-starter-log`（不会随 web 模块自动传递）。

2. **确认未关闭文件日志**

```yaml
wz:
  log:
    file:
      enabled: true   # 默认值
```

3. **日志文件在首次写入时才创建**

应用启动后需有日志输出（如 `INFO` 级别启动日志），才会在 `logs/` 目录下生成文件。路径相对于**进程工作目录**（IDE 运行为项目根目录）。

4. **检查实际路径**

默认文件位置：`logs/{spring.application.name}.log`，未配置 `spring.application.name` 时为 `logs/app.log`。

5. **与业务 logging 配置冲突**

若业务项目已设置 `logging.file.name`，Starter 会以更高优先级覆盖（`wz.log.file.enabled=true` 时）。如需自行管理，设置 `wz.log.file.enabled: false`。

## 本地开发

开发环境可关闭文件日志，使用 Spring Boot 默认控制台输出：

```yaml
# application-dev.yml
wz:
  log:
    file:
      enabled: false
```

## 模块职责

| 能力 | 状态 |
|------|------|
| 文件滚动（日期 + 大小） | ✅ 已实现 |
| 归档 gzip 压缩 | ✅ 已实现 |
| TraceId 链路追踪 | 🚧 规划中 |
| 操作审计 AOP | 🚧 规划中 |
