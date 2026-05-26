# 架构设计

## 整体架构

```
┌─────────────────────────────────────────────────┐
│                  用户业务项目                       │
│  api / service / dal / starter                  │
└──────────────────────┬──────────────────────────┘
                       │ 引入依赖
┌──────────────────────▼──────────────────────────┐
│              wz-spring-boot-starter              │
│  ┌─────────┐ ┌──────┐ ┌───────┐ ┌──────────┐   │
│  │  web    │ │mybatis│ │ redis │ │ security │   │
│  └────┬────┘ └───┬───┘ └───┬───┘ └────┬─────┘   │
│       └──────────┼─────────┼──────────┘         │
│                  ▼         ▼                     │
│            wz-spring-boot-starter-core           │
│                  ▼                               │
│               wz-common                          │
└─────────────────────────────────────────────────┘
                       ▲
                       │ 版本管理
              wz-dependencies (BOM)
```

## 模块依赖关系

依赖方向严格单向，禁止循环依赖：

```
common ← core ← web ← security
              ← mybatis / redis / log / oss / mq / job / monitor
              ← test (依赖 web)
```

## Starter 内部标准结构

每个 Starter 模块统一采用以下包结构：

```
com.kwz.starter.{module}/
├── autoconfigure/          # @AutoConfiguration 入口
│   └── WzXxxAutoConfiguration.java
├── properties/             # @ConfigurationProperties(prefix = "wz.xxx")
│   └── WzXxxProperties.java
├── core/                   # 核心实现（Service、Template）
├── support/                # 辅助类、拦截器、Filter
└── spi/                    # 扩展接口（可选）

META-INF/spring/
└── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

## 自动配置模式

```java
@AutoConfiguration
@EnableConfigurationProperties(WzWebProperties.class)
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "wz.web", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class WzWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
```

设计要点：

- `matchIfMissing = true` — 默认开启，无需显式配置
- `@ConditionalOnMissingBean` — 业务项目可注册同名 Bean 覆盖默认实现
- `wz.xxx.enabled=false` — 可通过配置关闭整个模块

## 业务项目推荐分层

```
用户业务项目
├── api/          对外接口定义（DTO、VO、Feign）
├── service/      业务逻辑（不依赖 Web）
├── dal/          数据访问（Entity、Mapper、Repository）
└── starter/      启动入口 + application.yml
         │
         └── 依赖 wz-spring-boot-starter-*
```

Starter 只负责横切能力，业务分层由团队规范或 Archetype 脚手架保证。

## 版本管理

项目使用 `${revision}` 统一版本号，配合 `flatten-maven-plugin` 在发布时将占位符解析为实际版本：

```xml
<!-- 源码 pom.xml -->
<version>${revision}</version>

<!-- 发布后 pom.xml -->
<version>1.0.0-SNAPSHOT</version>
```

详见 [构建与发布](/development/build)。
