package com.kwz.starter.websocket.handshake;

import com.kwz.starter.websocket.model.WebSocketPrincipal;
import com.kwz.starter.websocket.properties.WzWebSocketProperties;
import com.kwz.starter.websocket.spi.WebSocketHandshakeAuthenticator;
import com.kwz.starter.websocket.support.WzWebSocketAttributes;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

public class WzWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final WzWebSocketProperties properties;
    private final WebSocketHandshakeAuthenticator authenticator;

    public WzWebSocketHandshakeInterceptor(WzWebSocketProperties properties,
                                             WebSocketHandshakeAuthenticator authenticator) {
        this.properties = properties;
        this.authenticator = authenticator;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        if (!properties.getAuth().isEnabled()) {
            return true;
        }
        Optional<WebSocketPrincipal> principal = authenticator.authenticate(request, response);
        if (principal.isEmpty()) {
            return false;
        }
        attributes.put(WzWebSocketAttributes.PRINCIPAL, principal.get());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }

    static String extractQueryParam(ServerHttpRequest request, String paramName) {
        String query = request.getURI().getQuery();
        if (!StringUtils.hasText(query)) {
            return null;
        }
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            if (paramName.equals(pair.substring(0, idx))) {
                return pair.substring(idx + 1);
            }
        }
        return null;
    }
}
