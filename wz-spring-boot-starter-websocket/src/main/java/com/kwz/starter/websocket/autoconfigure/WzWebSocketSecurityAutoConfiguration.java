package com.kwz.starter.websocket.autoconfigure;

import com.kwz.starter.security.jwt.JwtService;
import com.kwz.starter.security.properties.WzSecurityProperties;
import com.kwz.starter.security.spi.TokenBlacklistService;
import com.kwz.starter.websocket.handshake.JwtWebSocketHandshakeAuthenticator;
import com.kwz.starter.websocket.properties.WzWebSocketProperties;
import com.kwz.starter.websocket.spi.WebSocketHandshakeAuthenticator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@AutoConfiguration(after = WzWebSocketAutoConfiguration.class)
@ConditionalOnClass(JwtService.class)
@ConditionalOnProperty(prefix = "wz.websocket.auth", name = "enabled", havingValue = "true")
public class WzWebSocketSecurityAutoConfiguration {

    @org.springframework.context.annotation.Bean
    @ConditionalOnMissingBean(WebSocketHandshakeAuthenticator.class)
    public WebSocketHandshakeAuthenticator jwtWebSocketHandshakeAuthenticator(
            WzWebSocketProperties properties,
            WzSecurityProperties securityProperties,
            JwtService jwtService,
            ObjectProvider<TokenBlacklistService> tokenBlacklistService) {
        return new JwtWebSocketHandshakeAuthenticator(
                properties, securityProperties, jwtService, tokenBlacklistService);
    }
}
