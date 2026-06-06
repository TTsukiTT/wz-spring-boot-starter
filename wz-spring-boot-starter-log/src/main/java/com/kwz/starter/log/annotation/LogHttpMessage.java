package com.kwz.starter.log.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 Controller 类或方法打印 HTTP 请求/响应报文。
 * <p>
 * 未标注时默认只记录一行访问日志；标注后按标准 HTTP 报文格式输出 body。
 * 若全局开启 {@code wz.log.request.log-payload=true}，则所有接口均打印报文。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogHttpMessage {

    /** 是否打印请求体 */
    boolean request() default true;

    /** 是否打印响应体 */
    boolean response() default true;
}
