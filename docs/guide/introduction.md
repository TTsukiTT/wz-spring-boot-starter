# 项目介绍

WZ Spring Boot Starter 是一套面向企业级 Java 项目的 **Spring Boot Starter 套件**，目标是让用户在任何新项目中快速启动，并将通用横切能力封装为可复用模块。

## 解决的问题

新建 Spring Boot 项目时，团队通常需要重复搭建以下基础设施：

- 统一 API 响应格式
- 全局异常处理
- 参数校验与错误码规范
- 数据库分页、审计字段
- Redis 缓存与分布式锁
- JWT 认证与权限控制
- 链路追踪与操作日志
- 文件存储、消息队列、定时任务

WZ Starter 将这些能力封装为独立模块，通过 **引入依赖 + 少量配置** 即可启用。

## 技术栈

| 组件 | 版本 |
|------|------|
| Java | 17+ |
| Spring Boot | 3.4.5 |
| MyBatis-Plus | 3.5.9 |
| Redisson | 3.41.0 |
| Hutool | 5.8.35 |
| MapStruct | 1.6.3 |

## 设计理念

| 原则 | 说明 |
|------|------|
| 约定优于配置 | 零配置即可运行，80% 场景开箱即用 |
| 按需装配 | 模块化 Starter，用户只引入需要的模块 |
| 分层解耦 | common（纯 Java）→ core（Spring 基础）→ 能力 Starter |
| 可扩展 | SPI + `@ConditionalOnMissingBean`，业务可覆盖默认实现 |
| 快速创建 | BOM 统一版本 + Maven Archetype 一键脚手架 |

## 项目结构

```
wz-spring-boot-starter/
├── wz-dependencies/              # BOM，统一依赖版本
├── wz-common/                    # 无 Spring 依赖：Result、异常、工具类
├── wz-spring-boot-starter-core/  # 核心自动配置、全局 wz.* 属性
├── wz-spring-boot-starter-web/   # 全局异常、CORS、参数校验
├── wz-spring-boot-starter-mybatis/
├── wz-spring-boot-starter-redis/
├── wz-spring-boot-starter-security/
├── wz-spring-boot-starter-log/
├── wz-spring-boot-starter-oss/
├── wz-spring-boot-starter-mq/
├── wz-spring-boot-starter-job/
├── wz-spring-boot-starter-monitor/
├── wz-spring-boot-starter-test/
└── docs/                         # VitePress 文档站点
```

## 实施进度

| 阶段 | 内容 | 状态 |
|------|------|------|
| P0 | common + core + web | ✅ 已完成 |
| P1 | mybatis + redis + log | 🚧 进行中（log 文件滚动 ✅） |
| P2 | security + monitor | 🚧 规划中 |
| P3 | oss + mq + job | 🚧 规划中 |
| P4 | test + archetype 脚手架 | 🚧 规划中 |
