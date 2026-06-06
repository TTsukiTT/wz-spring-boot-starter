package com.kwz.starter.websocket.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwz.starter.websocket.message.WzWebSocketMessage;
import com.kwz.starter.websocket.model.WebSocketPrincipal;
import com.kwz.starter.websocket.support.WzWebSocketAttributes;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WzWebSocketSessionManagerTest {

    @Test
    void shouldSendToUserAndBroadcast() throws Exception {
        WzWebSocketSessionManager manager = new WzWebSocketSessionManager(new ObjectMapper());

        WebSocketSession session1 = mockSession("s1", "100");
        WebSocketSession session2 = mockSession("s2", "200");
        manager.register(session1);
        manager.register(session2);

        manager.sendToUser("100", WzWebSocketMessage.of("notify", "hello"));
        verify(session1, times(1)).sendMessage(org.mockito.ArgumentMatchers.any(TextMessage.class));

        manager.broadcast(WzWebSocketMessage.pong());
        verify(session1, times(2)).sendMessage(org.mockito.ArgumentMatchers.any(TextMessage.class));
        verify(session2, times(1)).sendMessage(org.mockito.ArgumentMatchers.any(TextMessage.class));

        assertThat(manager.getOnlineCount()).isEqualTo(2);
        assertThat(manager.getOnlineUserIds()).containsExactly("100", "200");
    }

    @Test
    void shouldCloseSessionWhenPingTimeout() throws Exception {
        WzWebSocketSessionManager manager = new WzWebSocketSessionManager(new ObjectMapper());
        WebSocketSession session = mockSession("s1", "100");
        manager.register(session);
        manager.setLastPingAt("s1", System.currentTimeMillis() - 2000L);

        int closed = manager.closeIdleSessions(1000L);
        assertThat(closed).isEqualTo(1);
        verify(session).close(WzWebSocketSessionManager.PING_TIMEOUT);
        assertThat(manager.getOnlineCount()).isZero();
    }

    private static WebSocketSession mockSession(String sessionId, String userId) throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(sessionId);
        when(session.isOpen()).thenReturn(true);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(WzWebSocketAttributes.PRINCIPAL, WebSocketPrincipal.builder()
                .userId(userId)
                .username("user" + userId)
                .build());
        when(session.getAttributes()).thenReturn(attributes);
        return session;
    }
}
