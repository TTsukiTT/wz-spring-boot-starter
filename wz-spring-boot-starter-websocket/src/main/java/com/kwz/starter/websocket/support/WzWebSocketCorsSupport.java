package com.kwz.starter.websocket.support;

import com.kwz.starter.websocket.properties.WzWebSocketProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;

/**
 * WebSocket / SockJS 跨域配置：使用 Origin 模式，避免 {@code allowedOrigins="*"} 与 allowCredentials 冲突
 */
public final class WzWebSocketCorsSupport {

    private WzWebSocketCorsSupport() {
    }

    public static void applyAllowedOrigins(WebSocketHandlerRegistration registration,
                                           WzWebSocketProperties.Endpoint endpoint) {
        String[] patterns = resolveAllowedOriginPatterns(endpoint);
        registration.setAllowedOriginPatterns(patterns);
    }

    static String[] resolveAllowedOriginPatterns(WzWebSocketProperties.Endpoint endpoint) {
        if (endpoint.getAllowedOriginPatterns() != null && endpoint.getAllowedOriginPatterns().length > 0) {
            return endpoint.getAllowedOriginPatterns();
        }
        if (endpoint.getAllowedOrigins() != null && endpoint.getAllowedOrigins().length > 0) {
            return endpoint.getAllowedOrigins();
        }
        return new String[] {"*"};
    }

    /**
     * 是否包含字面量 {@code *}（应走 OriginPatterns 而非 Origins）
     */
    public static boolean containsWildcard(String[] values) {
        if (values == null) {
            return false;
        }
        for (String value : values) {
            if ("*".equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasTextValues(String[] values) {
        if (values == null || values.length == 0) {
            return false;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return true;
            }
        }
        return false;
    }
}
