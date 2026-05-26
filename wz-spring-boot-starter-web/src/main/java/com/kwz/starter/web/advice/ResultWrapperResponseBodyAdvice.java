package com.kwz.starter.web.advice;

import com.kwz.common.annotation.NoWrapResult;
import com.kwz.common.result.Result;
import com.kwz.starter.web.properties.WzWebProperties;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.InputStream;

/**
 * 将 Controller 直接返回的对象自动包装为 {@link Result}
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ResultWrapperResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final WzWebProperties webProperties;

    public ResultWrapperResponseBodyAdvice(WzWebProperties webProperties) {
        this.webProperties = webProperties;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (!webProperties.getWrapResult().isEnabled()) {
            return false;
        }
        return !shouldSkipReturnType(returnType);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof Result) {
            return body;
        }
        if (shouldSkipByContentType(selectedContentType)) {
            return body;
        }
        return Result.ok(body);
    }

    private boolean shouldSkipReturnType(MethodParameter returnType) {
        if (returnType.hasMethodAnnotation(NoWrapResult.class)) {
            return true;
        }
        if (returnType.getDeclaringClass().isAnnotationPresent(NoWrapResult.class)) {
            return true;
        }

        Class<?> returnClass = returnType.getParameterType();
        if (Result.class.isAssignableFrom(returnClass)) {
            return true;
        }
        if (org.springframework.http.ResponseEntity.class.isAssignableFrom(returnClass)) {
            return true;
        }
        return isRawBodyType(returnClass);
    }

    private boolean isRawBodyType(Class<?> type) {
        return type == byte[].class
                || InputStream.class.isAssignableFrom(type)
                || Resource.class.isAssignableFrom(type);
    }

    private boolean shouldSkipByContentType(MediaType selectedContentType) {
        if (selectedContentType == null) {
            return false;
        }
        return MediaType.APPLICATION_OCTET_STREAM.includes(selectedContentType)
                || MediaType.TEXT_EVENT_STREAM.includes(selectedContentType);
    }
}
