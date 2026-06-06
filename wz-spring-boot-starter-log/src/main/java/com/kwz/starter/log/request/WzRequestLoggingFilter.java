package com.kwz.starter.log.request;

import com.kwz.starter.log.properties.WzLogProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * HTTP 请求访问日志 Filter。
 * <p>
 * 在请求完成后记录 method、uri、状态码、耗时、客户端 IP 等信息；
 * traceId 由 TLog Filter 写入 MDC，会随日志格式 {@code %X{traceId}} 一并输出。
 */
public class WzRequestLoggingFilter extends OncePerRequestFilter {

    static final Logger ACCESS_LOG = LoggerFactory.getLogger("com.kwz.starter.log.access");
    static final String REQUEST_START_TIME = "wz.log.requestStartTime";

    private final WzLogProperties properties;

    public WzRequestLoggingFilter(WzLogProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return WzRequestLogSupport.isExcluded(request.getRequestURI(), properties.getRequest().getExcludePatterns());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startNanos = System.nanoTime();
        request.setAttribute(REQUEST_START_TIME, startNanos);
        try {
            filterChain.doFilter(request, response);
        } finally {
            logAccess(request, response, startNanos);
        }
    }

    private void logAccess(HttpServletRequest request, HttpServletResponse response, long startNanos) {
        WzLogProperties.Request requestProperties = properties.getRequest();
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000L;

        StringBuilder message = new StringBuilder(128);
        message.append(request.getMethod()).append(' ').append(request.getRequestURI());
        if (requestProperties.isIncludeQueryString()) {
            String queryString = WzRequestLogSupport.resolveQueryString(request);
            if (StringUtils.hasText(queryString)) {
                message.append('?').append(queryString);
            }
        }
        message.append(" status=").append(response.getStatus());
        message.append(" duration=").append(durationMs).append("ms");

        if (requestProperties.isIncludeClientInfo()) {
            message.append(" client=").append(WzRequestLogSupport.resolveClientIp(request));
        }
        if (requestProperties.isIncludeUserAgent()) {
            String userAgent = request.getHeader("User-Agent");
            if (StringUtils.hasText(userAgent)) {
                message.append(" ua=\"").append(WzRequestLogSupport.truncate(userAgent, 200)).append('"');
            }
        }

        if (requestProperties.getSlowThresholdMs() > 0 && durationMs >= requestProperties.getSlowThresholdMs()) {
            ACCESS_LOG.warn(message.toString());
        } else {
            ACCESS_LOG.info(message.toString());
        }
    }
}
