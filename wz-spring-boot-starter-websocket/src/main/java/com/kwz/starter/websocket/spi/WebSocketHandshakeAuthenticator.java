package com.kwz.starter.websocket.spi;

import com.kwz.starter.websocket.model.WebSocketPrincipal;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.util.Optional;

/**
 * WebSocket 握手鉴权 SPI，业务可自定义实现
 */
@FunctionalInterface
public interface WebSocketHandshakeAuthenticator {

    Optional<WebSocketPrincipal> authenticate(ServerHttpRequest request, ServerHttpResponse response);
}
