package com.kwz.starter.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明接口免 JWT 认证（白名单），可标注在 Controller 类或方法上。
 * <p>
 * 与 {@code wz.security.whitelist} 配置合并生效。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PermitAll {
}
