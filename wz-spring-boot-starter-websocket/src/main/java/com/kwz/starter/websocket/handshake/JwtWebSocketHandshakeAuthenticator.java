package com.kwz.starter.websocket.handshake;

import com.kwz.starter.security.jwt.JwtService;
import com.kwz.starter.security.jwt.ParsedAccessToken;
import com.kwz.starter.security.model.LoginUser;
import com.kwz.starter.security.properties.WzSecurityProperties;
import com.kwz.starter.security.spi.TokenBlacklistService;
import com.kwz.starter.websocket.model.WebSocketPrincipal;
import com.kwz.starter.websocket.properties.WzWebSocketProperties;
import com.kwz.starter.websocket.spi.WebSocketHandshakeAuthenticator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * 基于 JWT 的 WebSocket 握手鉴权（需引入 security 模块）
 */
public class JwtWebSocketHandshakeAuthenticator implements WebSocketHandshakeAuthenticator {

    private final WzWebSocketProperties properties;
    private final WzSecurityProperties securityProperties;
    private final JwtService jwtService;
    private final ObjectProvider<TokenBlacklistService> tokenBlacklistService;

    public JwtWebSocketHandshakeAuthenticator(WzWebSocketProperties properties,
                                              WzSecurityProperties securityProperties,
                                              JwtService jwtService,
                                              ObjectProvider<TokenBlacklistService> tokenBlacklistService) {
        this.properties = properties;
        this.securityProperties = securityProperties;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public Optional<WebSocketPrincipal> authenticate(ServerHttpRequest request, ServerHttpResponse response) {
        String rawToken = WzWebSocketHandshakeInterceptor.extractQueryParam(
                request, properties.getAuth().getTokenParam());
        if (!StringUtils.hasText(rawToken)) {
            return Optional.empty();
        }
        String token = stripBearer(rawToken);
        try {
            ParsedAccessToken parsed = jwtService.parseAccessTokenPayload(token);
            TokenBlacklistService blacklistService = tokenBlacklistService.getIfAvailable();
            if (blacklistService != null && blacklistService.isBlocked(parsed)) {
                return Optional.empty();
            }
            LoginUser loginUser = parsed.getLoginUser();
            return Optional.of(WebSocketPrincipal.builder()
                    .userId(String.valueOf(loginUser.getUserId()))
                    .username(loginUser.getUsername())
                    .attributes(Map.of("loginUser", loginUser))
                    .build());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private String stripBearer(String token) {
        String prefix = securityProperties.getJwt().getPrefix();
        if (StringUtils.hasText(prefix) && token.startsWith(prefix)) {
            return token.substring(prefix.length()).trim();
        }
        return token.trim();
    }
}
