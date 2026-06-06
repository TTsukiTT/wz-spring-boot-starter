package com.kwz.starter.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = WzWebSocketSockJsConfigurationTest.TestApplication.class,
        properties = {
                "wz.security.jwt.secret=abcdefghijklmnopqrstuvwxyz1234567890",
                "wz.websocket.auth.enabled=false",
                "wz.websocket.sockjs.enabled=true"
        })
class WzWebSocketSockJsConfigurationTest {

    @Test
    void shouldStartWithSockJsEnabled(org.springframework.context.ApplicationContext context) {
        assertThat(context.getBeansOfType(WebSocketConfigurer.class)).isNotEmpty();
    }

    @SpringBootApplication
    static class TestApplication {
    }
}
