package com.kwz.starter.web.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwz.common.result.Result;
import com.kwz.starter.web.properties.WzWebProperties;
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
 * String 返回类型走 {@link org.springframework.http.converter.StringHttpMessageConverter}，
 * 需在 i18n 解析后将 Result 序列化为 JSON 字符串。
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 200)
public class ResultStringResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;
    private final WzWebProperties webProperties;

    public ResultStringResponseBodyAdvice(ObjectMapper objectMapper, WzWebProperties webProperties) {
        this.objectMapper = objectMapper;
        this.webProperties = webProperties;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return webProperties.getWrapResult().isEnabled()
                && String.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (!(body instanceof Result<?> result)) {
            return body;
        }
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize Result", e);
        }
    }
}
