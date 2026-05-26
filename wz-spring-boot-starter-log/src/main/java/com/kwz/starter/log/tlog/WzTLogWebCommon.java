package com.kwz.starter.log.tlog;

import com.yomahub.tlog.constant.TLogConstants;
import com.yomahub.tlog.core.rpc.TLogLabelBean;
import com.yomahub.tlog.core.rpc.TLogRPCHandler;
import jakarta.servlet.http.HttpServletRequest;

/**
 * TLog Web 预处理（Jakarta Servlet 版）。
 * <p>
 * 原 {@code TLogWebCommon} 使用 {@code javax.servlet}，Spring Boot 3 无法调用。
 */
public final class WzTLogWebCommon extends TLogRPCHandler {

    private static volatile WzTLogWebCommon instance;

    private WzTLogWebCommon() {
    }

    public static WzTLogWebCommon loadInstance() {
        if (instance == null) {
            synchronized (WzTLogWebCommon.class) {
                if (instance == null) {
                    instance = new WzTLogWebCommon();
                }
            }
        }
        return instance;
    }

    public void preHandle(HttpServletRequest request) {
        TLogLabelBean labelBean = new TLogLabelBean(
                request.getHeader(TLogConstants.PRE_IVK_APP_KEY),
                request.getHeader(TLogConstants.PRE_IVK_APP_HOST),
                request.getHeader(TLogConstants.PRE_IP_KEY),
                request.getHeader(TLogConstants.TLOG_TRACE_KEY),
                request.getHeader(TLogConstants.TLOG_SPANID_KEY)
        );
        processProviderSide(labelBean);
    }

    public void afterCompletion() {
        cleanThreadLocal();
    }
}
