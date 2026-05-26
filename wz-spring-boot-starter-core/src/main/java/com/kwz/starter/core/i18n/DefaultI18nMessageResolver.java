package com.kwz.starter.core.i18n;

import com.kwz.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 基于 Spring MessageSource 的默认消息解析器
 */
@RequiredArgsConstructor
public class DefaultI18nMessageResolver implements I18nMessageResolver {

    private final MessageSource messageSource;

    @Override
    public String resolve(String code, String defaultMessage, Object... args) {
        return messageSource.getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    @Override
    public String resolve(ErrorCode errorCode, Object... args) {
        return resolve(errorCode.getMessageKey(), errorCode.getMessage(), args);
    }
}
