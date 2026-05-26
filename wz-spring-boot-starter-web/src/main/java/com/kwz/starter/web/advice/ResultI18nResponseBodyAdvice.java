package com.kwz.starter.web.advice;

import com.kwz.common.result.Result;
import com.kwz.starter.core.i18n.I18nMessageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一解析 Result.message 多语言文案
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RequiredArgsConstructor
public class ResultI18nResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final I18nMessageResolver i18nMessageResolver;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof Result<?> result) {
            resolveMessage(result);
        }
        return body;
    }

    private void resolveMessage(Result<?> result) {
        if (!result.isResolveI18n()) {
            return;
        }
        String key = result.getMessageKey() != null
                ? result.getMessageKey()
                : "wz.error." + result.getCode();
        String resolved = i18nMessageResolver.resolve(key, result.getMessage(), result.getMessageArgs());
        result.setMessage(resolved);
    }
}
