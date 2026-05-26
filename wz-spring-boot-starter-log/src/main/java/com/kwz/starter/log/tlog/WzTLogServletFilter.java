package com.kwz.starter.log.tlog;

import com.kwz.starter.log.properties.WzLogProperties;
import com.yomahub.tlog.constant.TLogConstants;
import com.yomahub.tlog.context.TLogContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Spring Boot 3 下 TLog 的 Jakarta Servlet Filter 适配。
 * <p>
 * 参考：<a href="https://juejin.cn/post/7408202391504371727">SpringBoot3 适配 TLog</a>
 */
public class WzTLogServletFilter implements Filter {

    private final WzLogProperties properties;

    public WzTLogServletFilter(WzLogProperties properties) {
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest
                && response instanceof HttpServletResponse httpResponse) {
            try {
                WzTLogWebCommon.loadInstance().preHandle(httpRequest);
                syncTraceIdToMdc(httpResponse);
                chain.doFilter(request, response);
                return;
            } finally {
                clearTraceIdFromMdc();
                WzTLogWebCommon.loadInstance().afterCompletion();
            }
        }
        chain.doFilter(request, response);
    }

    private void syncTraceIdToMdc(HttpServletResponse response) {
        String traceId = TLogContext.getTraceId();
        if (!StringUtils.hasText(traceId)) {
            return;
        }
        String mdcKey = properties.getTrace().getMdcKey();
        MDC.put(mdcKey, traceId);
        MDC.put(TLogConstants.TLOG_TRACE_KEY, traceId);
        response.addHeader(TLogConstants.TLOG_TRACE_KEY, traceId);
    }

    private void clearTraceIdFromMdc() {
        MDC.remove(properties.getTrace().getMdcKey());
        MDC.remove(TLogConstants.TLOG_TRACE_KEY);
    }
}
