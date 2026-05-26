package com.kwz.starter.log.tlog;

import com.kwz.starter.log.properties.WzLogProperties;
import com.yomahub.tlog.constant.TLogConstants;
import com.yomahub.tlog.context.TLogContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC 链路追踪拦截器（Jakarta），作为 Filter 的补充。
 */
public class WzTLogHandlerInterceptor implements HandlerInterceptor {

    private final WzLogProperties properties;

    public WzTLogHandlerInterceptor(WzLogProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!StringUtils.hasText(TLogContext.getTraceId())) {
            WzTLogWebCommon.loadInstance().preHandle(request);
        }
        String traceId = TLogContext.getTraceId();
        if (StringUtils.hasText(traceId)) {
            MDC.put(properties.getTrace().getMdcKey(), traceId);
            MDC.put(TLogConstants.TLOG_TRACE_KEY, traceId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // TLog 上下文清理由 WzTLogServletFilter 统一负责
    }
}
