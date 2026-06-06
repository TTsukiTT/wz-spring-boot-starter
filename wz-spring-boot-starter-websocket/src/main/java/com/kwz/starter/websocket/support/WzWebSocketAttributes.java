package com.kwz.starter.websocket.support;

import com.kwz.starter.websocket.model.WebSocketPrincipal;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

public final class WzWebSocketAttributes {

    public static final String PRINCIPAL = "wz.websocket.principal";

    private WzWebSocketAttributes() {
    }

    public static Optional<WebSocketPrincipal> getPrincipal(WebSocketSession session) {
        Object value = session.getAttributes().get(PRINCIPAL);
        if (value instanceof WebSocketPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static Optional<String> getUserId(WebSocketSession session) {
        return getPrincipal(session).map(WebSocketPrincipal::getUserId);
    }
}
