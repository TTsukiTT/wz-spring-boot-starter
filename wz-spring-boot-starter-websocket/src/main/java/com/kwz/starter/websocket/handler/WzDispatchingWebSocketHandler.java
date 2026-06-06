package com.kwz.starter.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwz.starter.websocket.message.WzWebSocketMessage;
import com.kwz.starter.websocket.session.WzWebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

/**
 * 统一 WebSocket 入口：内置 ping/pong，按 type 分发给 {@link WzWebSocketMessageListener}
 */
public class WzDispatchingWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WzDispatchingWebSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final WzWebSocketSessionManager sessionManager;
    private final List<WzWebSocketMessageListener> listeners;

    public WzDispatchingWebSocketHandler(ObjectMapper objectMapper,
                                         WzWebSocketSessionManager sessionManager,
                                         List<WzWebSocketMessageListener> listeners) {
        this.objectMapper = objectMapper;
        this.sessionManager = sessionManager;
        this.listeners = listeners;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionManager.register(session);
        log.info("WebSocket connected: sessionId={}, userId={}",
                session.getId(),
                session.getAttributes().get(com.kwz.starter.websocket.support.WzWebSocketAttributes.PRINCIPAL));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WzWebSocketMessage payload = parseMessage(message.getPayload());
        if (payload == null || !StringUtils.hasText(payload.getType())) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                    WzWebSocketMessage.error("invalid message format"))));
            return;
        }
        if (WzWebSocketMessage.TYPE_PING.equals(payload.getType())) {
            sessionManager.recordPing(session);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(WzWebSocketMessage.pong())));
            return;
        }
        for (WzWebSocketMessageListener listener : listeners) {
            if (listener.supports(payload.getType())) {
                listener.onMessage(session, payload);
                return;
            }
        }
        log.debug("No listener for websocket type: {}", payload.getType());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.remove(session);
        log.info("WebSocket closed: sessionId={}, status={}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("WebSocket transport error: sessionId={}", session.getId(), exception);
        sessionManager.remove(session);
    }

    private WzWebSocketMessage parseMessage(String payload) {
        try {
            return objectMapper.readValue(payload, WzWebSocketMessage.class);
        } catch (Exception ex) {
            log.debug("Failed to parse websocket message: {}", payload, ex);
            return null;
        }
    }
}
