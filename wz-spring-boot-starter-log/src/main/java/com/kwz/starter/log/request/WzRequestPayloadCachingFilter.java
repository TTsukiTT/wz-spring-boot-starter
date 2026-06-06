package com.kwz.starter.log.request;

import com.kwz.starter.log.properties.WzLogProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * 为需要打印报文的请求包装可重复读取的 request/response。
 */
public class WzRequestPayloadCachingFilter extends OncePerRequestFilter {

    private final WzLogProperties properties;
    private final WzPayloadLoggingPathRegistry pathRegistry;
    private final ApplicationContext applicationContext;

    public WzRequestPayloadCachingFilter(WzLogProperties properties,
                                         WzPayloadLoggingPathRegistry pathRegistry,
                                         ApplicationContext applicationContext) {
        this.properties = properties;
        this.pathRegistry = pathRegistry;
        this.applicationContext = applicationContext;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return WzRequestLogSupport.isExcluded(request.getRequestURI(), properties.getRequest().getExcludePatterns());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!shouldCachePayload(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        int cacheLimit = Math.max(properties.getRequest().getMaxPayloadLength(), 4096);
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, cacheLimit);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        request.setAttribute(WzHttpMessageLogConstants.CACHING_REQUEST, wrappedRequest);
        request.setAttribute(WzHttpMessageLogConstants.CACHING_RESPONSE, wrappedResponse);
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            wrappedResponse.copyBodyToResponse();
        }
    }

    private boolean shouldCachePayload(HttpServletRequest request) {
        WzLogProperties.Request requestProperties = properties.getRequest();
        if (requestProperties.isLogPayload()) {
            return true;
        }
        pathRegistry.initializeIfNecessary(applicationContext);
        if (pathRegistry.hasAnnotatedEndpoints()) {
            // 存在 @LogHttpMessage 时统一包装，避免路径匹配偏差导致 body 无法缓存
            return true;
        }
        return pathRegistry.matches(request.getMethod(), request.getRequestURI());
    }
}
