package com.kwz.starter.websocket;

import com.kwz.starter.websocket.session.WzWebSocketSessionManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = WzWebSocketAutoConfigurationTest.TestApplication.class,
        properties = {
                "wz.security.jwt.secret=abcdefghijklmnopqrstuvwxyz1234567890",
                "wz.websocket.auth.enabled=false"
        })
class WzWebSocketAutoConfigurationTest {

    @Test
    void shouldRegisterWebSocketBeans(org.springframework.context.ApplicationContext context) {
        assertThat(context.getBeansOfType(WebSocketConfigurer.class)).isNotEmpty();
        assertThat(context.getBean(WebSocketHandler.class)).isNotNull();
        assertThat(context.getBean(WzWebSocketSessionManager.class)).isNotNull();
    }

    @SpringBootApplication
    static class TestApplication {
    }
}
