package com.kwz.starter.websocket.handler;

import com.kwz.starter.websocket.message.WzWebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * 按消息类型处理 WebSocket 业务消息
 */
public interface WzWebSocketMessageListener {

    /**
     * 是否支持该消息类型
     */
    boolean supports(String type);

    /**
     * 处理消息，可通过 {@link com.kwz.starter.websocket.session.WzWebSocketSessionManager} 回推
     */
    void onMessage(WebSocketSession session, WzWebSocketMessage message) throws Exception;
}
