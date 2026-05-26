package com.kwz.starter.core.i18n;

import com.kwz.common.exception.ErrorCode;

/**
 * 透传消息解析器 — i18n 关闭时使用，直接返回默认文案
 */
public class PassthroughI18nMessageResolver implements I18nMessageResolver {

    @Override
    public String resolve(String code, String defaultMessage, Object... args) {
        return defaultMessage;
    }

    @Override
    public String resolve(ErrorCode errorCode, Object... args) {
        return errorCode.getMessage();
    }
}
