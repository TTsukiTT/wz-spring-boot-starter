package com.kwz.starter.websocket.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwz.starter.websocket.handler.WzDispatchingWebSocketHandler;
import com.kwz.starter.websocket.handler.WzWebSocketMessageListener;
import com.kwz.starter.websocket.handshake.WzWebSocketHandshakeInterceptor;
import com.kwz.starter.websocket.heartbeat.WzWebSocketIdleTimeoutChecker;
import com.kwz.starter.websocket.properties.WzWebSocketProperties;
import com.kwz.starter.websocket.session.WzWebSocketSessionManager;
import com.kwz.starter.websocket.spi.WebSocketHandshakeAuthenticator;
import com.kwz.starter.websocket.support.WzWebSocketCorsSupport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.SockJsServiceRegistration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Collections;
import java.util.List;

@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnClass(WebSocketHandler.class)
@ConditionalOnProperty(prefix = "wz.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WzWebSocketProperties.class)
@EnableWebSocket
public class WzWebSocketAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WzWebSocketSessionManager wzWebSocketSessionManager(ObjectMapper objectMapper) {
        return new WzWebSocketSessionManager(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "wz.websocket.heartbeat", name = "enabled", havingValue = "true", matchIfMissing = true)
    public WzWebSocketIdleTimeoutChecker wzWebSocketIdleTimeoutChecker(WzWebSocketSessionManager sessionManager,
                                                                       WzWebSocketProperties properties) {
        return new WzWebSocketIdleTimeoutChecker(sessionManager, properties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "wzDispatchingWebSocketHandler")
    public WebSocketHandler wzDispatchingWebSocketHandler(ObjectMapper objectMapper,
                                                          WzWebSocketSessionManager sessionManager,
                                                          ObjectProvider<List<WzWebSocketMessageListener>> listeners) {
        List<WzWebSocketMessageListener> resolved = listeners.getIfAvailable(Collections::emptyList);
        if (CollectionUtils.isEmpty(resolved)) {
            resolved = Collections.emptyList();
        }
        return new WzDispatchingWebSocketHandler(objectMapper, sessionManager, resolved);
    }

    @Bean
    @ConditionalOnMissingBean(WebSocketHandshakeAuthenticator.class)
    @ConditionalOnProperty(prefix = "wz.websocket.auth", name = "enabled", havingValue = "false", matchIfMissing = true)
    public WebSocketHandshakeAuthenticator noopWebSocketHandshakeAuthenticator() {
        return (request, response) -> java.util.Optional.empty();
    }

    @Bean
    @ConditionalOnMissingBean(WebSocketHandshakeAuthenticator.class)
    @ConditionalOnProperty(prefix = "wz.websocket.auth", name = "enabled", havingValue = "true")
    @ConditionalOnMissingClass("com.kwz.starter.security.jwt.JwtService")
    public WebSocketHandshakeAuthenticator rejectWebSocketHandshakeAuthenticator() {
        return (request, response) -> java.util.Optional.empty();
    }

    @Bean
    @ConditionalOnMissingBean
    public HandshakeInterceptor wzWebSocketHandshakeInterceptor(WzWebSocketProperties properties,
                                                                WebSocketHandshakeAuthenticator authenticator) {
        return new WzWebSocketHandshakeInterceptor(properties, authenticator);
    }

    @Bean
    public WebSocketConfigurer wzWebSocketConfigurer(WzWebSocketProperties properties,
                                                     WebSocketHandler webSocketHandler,
                                                     HandshakeInterceptor handshakeInterceptor) {
        return new WebSocketConfigurer() {
            @Override
            public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
                String path = resolveEndpointPath(properties);
                WebSocketHandlerRegistration registration = registry.addHandler(webSocketHandler, path)
                        .addInterceptors(handshakeInterceptor);
                WzWebSocketCorsSupport.applyAllowedOrigins(registration, properties.getEndpoint());
                if (properties.getSockjs().isEnabled()) {
                    SockJsServiceRegistration sockJs = registration.withSockJS();
                    WzWebSocketProperties.SockJs sockjs = properties.getSockjs();
                    sockJs.setHeartbeatTime(sockjs.getHeartbeatTime());
                    sockJs.setDisconnectDelay(sockjs.getDisconnectDelay());
                    sockJs.setSessionCookieNeeded(sockjs.isSessionCookieNeeded());
                }
            }

            private String resolveEndpointPath(WzWebSocketProperties properties) {
                if (properties.getSockjs().isEnabled()
                        && StringUtils.hasText(properties.getSockjs().getPath())) {
                    return properties.getSockjs().getPath();
                }
                return properties.getEndpoint().getPath();
            }
        };
    }
}
