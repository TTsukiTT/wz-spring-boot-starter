package com.kwz.starter.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket JSON 消息信封：{@code { "type": "...", "data": ... }}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WzWebSocketMessage {

    public static final String TYPE_PING = "ping";
    public static final String TYPE_PONG = "pong";
    public static final String TYPE_ERROR = "error";

    private String type;
    private Object data;

    public static WzWebSocketMessage ping() {
        return new WzWebSocketMessage(TYPE_PING, null);
    }

    public static WzWebSocketMessage pong() {
        return new WzWebSocketMessage(TYPE_PONG, null);
    }

    public static WzWebSocketMessage error(String message) {
        return new WzWebSocketMessage(TYPE_ERROR, message);
    }

    public static WzWebSocketMessage of(String type, Object data) {
        return new WzWebSocketMessage(type, data);
    }
}
