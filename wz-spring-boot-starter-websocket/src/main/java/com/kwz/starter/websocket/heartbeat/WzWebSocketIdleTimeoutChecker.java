package com.kwz.starter.websocket.heartbeat;

import com.kwz.starter.websocket.properties.WzWebSocketProperties;
import com.kwz.starter.websocket.session.WzWebSocketSessionManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 定时检测：超过配置时间未收到 ping 则关闭连接
 */
public class WzWebSocketIdleTimeoutChecker {

    private static final Logger log = LoggerFactory.getLogger(WzWebSocketIdleTimeoutChecker.class);

    private final WzWebSocketSessionManager sessionManager;
    private final WzWebSocketProperties properties;
    private ScheduledExecutorService scheduler;

    public WzWebSocketIdleTimeoutChecker(WzWebSocketSessionManager sessionManager,
                                       WzWebSocketProperties properties) {
        this.sessionManager = sessionManager;
        this.properties = properties;
    }

    @PostConstruct
    void start() {
        WzWebSocketProperties.Heartbeat heartbeat = properties.getHeartbeat();
        if (!heartbeat.isEnabled() || heartbeat.getIdleTimeoutSeconds() <= 0) {
            return;
        }
        long checkIntervalSeconds = Math.max(1, heartbeat.getCheckIntervalSeconds());
        long idleTimeoutMillis = heartbeat.getIdleTimeoutSeconds() * 1000L;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "wz-websocket-idle-check");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(
                () -> checkIdleSessions(idleTimeoutMillis),
                checkIntervalSeconds,
                checkIntervalSeconds,
                TimeUnit.SECONDS);
        log.info("WebSocket idle timeout enabled: idleTimeout={}s, checkInterval={}s",
                heartbeat.getIdleTimeoutSeconds(), checkIntervalSeconds);
    }

    @PreDestroy
    void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private void checkIdleSessions(long idleTimeoutMillis) {
        try {
            int closed = sessionManager.closeIdleSessions(idleTimeoutMillis);
            if (closed > 0) {
                log.info("WebSocket idle timeout closed {} session(s)", closed);
            }
        } catch (Exception ex) {
            log.warn("WebSocket idle timeout check failed", ex);
        }
    }
}
