package com.kwz.starter.core.i18n;

import com.kwz.common.exception.ErrorCode;

/**
 * 多语言消息解析器
 */
public interface I18nMessageResolver {

    /**
     * 按 key 解析消息，找不到时使用 defaultMessage
     */
    String resolve(String code, String defaultMessage, Object... args);

    /**
     * 按错误码解析消息，找不到时使用 ErrorCode 默认文案
     */
    String resolve(ErrorCode errorCode, Object... args);

    /**
     * 按 key 解析消息
     */
    default String resolve(String code, Object... args) {
        return resolve(code, code, args);
    }
}
