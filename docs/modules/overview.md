# 模块概览

WZ Starter 采用多模块 Maven 结构，每个模块职责单一、可独立引入。

## 模块列表

| 模块 | ArtifactId | 说明 | 状态 |
|------|-----------|------|------|
| BOM | `wz-dependencies` | 统一依赖版本管理 | ✅ |
| 公共层 | `wz-common` | 无 Spring 依赖的工具与模型 | ✅ |
| 核心 | `wz-spring-boot-starter-core` | 全局属性、基础自动配置 | ✅ |
| Web | `wz-spring-boot-starter-web` | 统一响应、全局异常、CORS | ✅ |
| MyBatis | `wz-spring-boot-starter-mybatis` | 分页、审计字段、多数据源 | 🚧 |
| Redis | `wz-spring-boot-starter-redis` | 缓存抽象、分布式锁 | ✅ |
| Security | `wz-spring-boot-starter-security` | JWT 认证、RBAC 权限 | 🚧 |
| Log | `wz-spring-boot-starter-log` | 文件滚动、压缩 | ✅ |
| OSS | `wz-spring-boot-starter-oss` | 文件存储抽象 | 🚧 |
| MQ | `wz-spring-boot-starter-mq` | 消息队列抽象 | 🚧 |
| Job | `wz-spring-boot-starter-job` | 定时任务 | 🚧 |
| Monitor | `wz-spring-boot-starter-monitor` | Actuator、Prometheus | 🚧 |
| Test | `wz-spring-boot-starter-test` | 测试工具 | 🚧 |

## 依赖关系

```
wz-dependencies (BOM)
       │
       ├── wz-common
       │       ↑
       ├── wz-spring-boot-starter-core
       │       ↑
       ├── wz-spring-boot-starter-web ──→ wz-spring-boot-starter-security
       ├── wz-spring-boot-starter-mybatis
       ├── wz-spring-boot-starter-redis
       ├── wz-spring-boot-starter-log
       ├── wz-spring-boot-starter-oss
       ├── wz-spring-boot-starter-mq
       ├── wz-spring-boot-starter-job
       ├── wz-spring-boot-starter-monitor
       └── wz-spring-boot-starter-test
```

## 常用组合

### 最小 Web 项目

```xml
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-web</artifactId>
</dependency>
```

### 标准 CRUD 项目

```xml
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-mybatis</artifactId>
</dependency>
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-redis</artifactId>
</dependency>
```

### 完整后端项目

在上述基础上追加 `security`、`log`、`monitor` 等模块。
