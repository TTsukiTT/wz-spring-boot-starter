package com.kwz.starter.websocket.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WebSocket 配置，前缀 wz.websocket.*
 */
@Data
@ConfigurationProperties(prefix = "wz.websocket")
public class WzWebSocketProperties {

    /** 是否启用 WebSocket 模块 */
    private boolean enabled = true;

    private Endpoint endpoint = new Endpoint();

    private Heartbeat heartbeat = new Heartbeat();

    private SockJs sockjs = new SockJs();

    private Auth auth = new Auth();

    @Data
    public static class Endpoint {

        /** WebSocket 路径 */
        private String path = "/ws";

        /**
         * 允许的 Origin 模式（推荐），支持 {@code *} 通配，与 allowCredentials / SockJS 兼容；未配置时默认 {@code *}
         */
        private String[] allowedOriginPatterns;

        /**
         * 已废弃，请使用 {@link #allowedOriginPatterns}；若仅配置此项也会自动按模式处理
         */
        private String[] allowedOrigins;
    }

    @Data
    public static class Heartbeat {

        /** 是否启用空闲检测（N 秒未收到 ping 则断开） */
        private boolean enabled = true;

        /** 未收到 ping 的最大间隔（秒），0 表示不限制 */
        private long idleTimeoutSeconds = 60;

        /** 空闲检测周期（秒），建议小于 idle-timeout-seconds */
        private long checkIntervalSeconds = 10;
    }

    @Data
    public static class SockJs {

        /** 是否启用 SockJS（浏览器降级、统一连接入口） */
        private boolean enabled = false;

        /**
         * SockJS 端点路径，留空使用 {@link Endpoint#path}
         */
        private String path;

        /** SockJS 协议层心跳间隔（毫秒） */
        private long heartbeatTime = 25_000;

        /** 客户端断开延迟（毫秒） */
        private long disconnectDelay = 5_000;

        /** 是否依赖 JSESSIONID Cookie */
        private boolean sessionCookieNeeded = false;
    }

    @Data
    public static class Auth {

        /** 握手时是否校验 Token */
        private boolean enabled = false;

        /** 握手 Query 参数中的 Token 名称 */
        private String tokenParam = "token";
    }
}
