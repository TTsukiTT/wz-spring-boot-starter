package com.kwz.starter.websocket.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwz.starter.websocket.message.WzWebSocketMessage;
import com.kwz.starter.websocket.support.WzWebSocketAttributes;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * WebSocket 会话管理：按 sessionId / userId 发送消息
 */
public class WzWebSocketSessionManager {

    static final CloseStatus PING_TIMEOUT = new CloseStatus(4001, "ping timeout");

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> userSessionIndex = new ConcurrentHashMap<>();
    private final Map<String, Long> lastPingAtMillis = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public WzWebSocketSessionManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void register(WebSocketSession session) {
        sessions.put(session.getId(), session);
        lastPingAtMillis.put(session.getId(), System.currentTimeMillis());
        WzWebSocketAttributes.getUserId(session).ifPresent(userId -> userSessionIndex.put(userId, session.getId()));
    }

    public void recordPing(WebSocketSession session) {
        lastPingAtMillis.put(session.getId(), System.currentTimeMillis());
    }

    void setLastPingAt(String sessionId, long timestampMillis) {
        lastPingAtMillis.put(sessionId, timestampMillis);
    }

    public void remove(WebSocketSession session) {
        sessions.remove(session.getId());
        lastPingAtMillis.remove(session.getId());
        WzWebSocketAttributes.getUserId(session).ifPresent(userSessionIndex::remove);
    }

    /**
     * 关闭超过空闲阈值的连接，返回本次关闭数量
     */
    public int closeIdleSessions(long idleTimeoutMillis) {
        if (idleTimeoutMillis <= 0) {
            return 0;
        }
        long now = System.currentTimeMillis();
        List<String> idleSessionIds = new ArrayList<>();
        for (Map.Entry<String, Long> entry : lastPingAtMillis.entrySet()) {
            if (now - entry.getValue() >= idleTimeoutMillis) {
                idleSessionIds.add(entry.getKey());
            }
        }
        int closed = 0;
        for (String sessionId : idleSessionIds) {
            WebSocketSession session = sessions.get(sessionId);
            if (session == null) {
                lastPingAtMillis.remove(sessionId);
                continue;
            }
            if (closeSession(session, PING_TIMEOUT)) {
                closed++;
            }
            remove(session);
        }
        return closed;
    }

    public int getOnlineCount() {
        return sessions.size();
    }

    public Collection<WebSocketSession> getAllSessions() {
        return sessions.values();
    }

    public Optional<WebSocketSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public Optional<WebSocketSession> getSessionByUserId(String userId) {
        String sessionId = userSessionIndex.get(userId);
        if (!StringUtils.hasText(sessionId)) {
            return Optional.empty();
        }
        return getSession(sessionId);
    }

    public void sendToSession(String sessionId, WzWebSocketMessage message) throws IOException {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            send(session, message);
        }
    }

    public void sendToUser(String userId, WzWebSocketMessage message) throws IOException {
        getSessionByUserId(userId).ifPresent(session -> {
            try {
                send(session, message);
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to send websocket message to user " + userId, ex);
            }
        });
    }

    public void broadcast(WzWebSocketMessage message) throws IOException {
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                send(session, message);
            }
        }
    }

    public void broadcastExcept(String excludeSessionId, WzWebSocketMessage message) throws IOException {
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            if (!entry.getKey().equals(excludeSessionId) && entry.getValue().isOpen()) {
                send(entry.getValue(), message);
            }
        }
    }

    public Collection<String> getOnlineUserIds() {
        return userSessionIndex.keySet().stream().sorted().collect(Collectors.toList());
    }

    private void send(WebSocketSession session, WzWebSocketMessage message) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
    }

    private static boolean closeSession(WebSocketSession session, CloseStatus status) {
        if (!session.isOpen()) {
            return false;
        }
        try {
            session.close(status);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }
}
