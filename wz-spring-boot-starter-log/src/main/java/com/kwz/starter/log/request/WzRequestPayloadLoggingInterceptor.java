package com.kwz.starter.log.request;

import com.kwz.starter.log.annotation.LogHttpMessage;
import com.kwz.starter.log.properties.WzLogProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * 在请求完成后按标准 HTTP 报文格式输出 body。
 */
public class WzRequestPayloadLoggingInterceptor implements HandlerInterceptor {

    static final Logger MESSAGE_LOG = LoggerFactory.getLogger("com.kwz.starter.log.message");

    private final WzLogProperties properties;

    public WzRequestPayloadLoggingInterceptor(WzLogProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        LogHttpMessage annotation = WzPayloadLoggingPathRegistry.resolveAnnotation(handlerMethod);
        if (annotation != null) {
            request.setAttribute(WzHttpMessageLogConstants.LOG_HTTP_MESSAGE, annotation);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                @Nullable Exception ex) {
        PayloadLoggingPolicy policy = resolveLoggingPolicy(request, handler);
        if (policy == null) {
            return;
        }

        ContentCachingResponseWrapper cachingResponse = (ContentCachingResponseWrapper) request.getAttribute(
                WzHttpMessageLogConstants.CACHING_RESPONSE);
        long durationMs = resolveDurationMs(request);
        String message = WzHttpMessageFormatter.format(
                request,
                response,
                cachingResponse,
                policy.logRequest(),
                policy.logResponse(),
                properties,
                durationMs);
        MESSAGE_LOG.info(message);
    }

    @Nullable
    private PayloadLoggingPolicy resolveLoggingPolicy(HttpServletRequest request, Object handler) {
        if (WzRequestLogSupport.isExcluded(request.getRequestURI(), properties.getRequest().getExcludePatterns())) {
            return null;
        }
        LogHttpMessage annotation = (LogHttpMessage) request.getAttribute(WzHttpMessageLogConstants.LOG_HTTP_MESSAGE);
        if (annotation == null && handler instanceof HandlerMethod handlerMethod) {
            annotation = WzPayloadLoggingPathRegistry.resolveAnnotation(handlerMethod);
        }
        if (annotation != null) {
            return new PayloadLoggingPolicy(annotation.request(), annotation.response());
        }
        if (properties.getRequest().isLogPayload()) {
            return PayloadLoggingPolicy.both();
        }
        return null;
    }

    private static long resolveDurationMs(HttpServletRequest request) {
        Object startTime = request.getAttribute(WzRequestLoggingFilter.REQUEST_START_TIME);
        if (startTime instanceof Long startNanos) {
            return (System.nanoTime() - startNanos) / 1_000_000L;
        }
        return 0L;
    }

    record PayloadLoggingPolicy(boolean logRequest, boolean logResponse) {

        static PayloadLoggingPolicy both() {
            return new PayloadLoggingPolicy(true, true);
        }
    }
}
