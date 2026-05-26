package com.kwz.starter.mybatis.handler;

/**
 * 审计字段用户 ID 提供者，业务项目可注册 Bean 覆盖（如对接 Security 模块）
 */
@FunctionalInterface
public interface AuditUserProvider {

    Long getCurrentUserId();
}
