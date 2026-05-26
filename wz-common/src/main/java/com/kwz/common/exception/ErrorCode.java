package com.kwz.common.exception;

/**
 * 错误码接口 — 各业务模块实现
 */
public interface ErrorCode {

    int getCode();

    String getMessage();

    /**
     * i18n 消息 key，默认 {@code wz.error.{code}}
     */
    default String getMessageKey() {
        return "wz.error." + getCode();
    }
}
