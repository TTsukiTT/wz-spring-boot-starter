package com.kwz.starter.websocket.model;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.Map;

/**
 * WebSocket 握手后写入 Session 的用户身份
 */
@Data
@Builder
public class WebSocketPrincipal {

    private String userId;
    private String username;
    @Builder.Default
    private Map<String, Object> attributes = Collections.emptyMap();
}
