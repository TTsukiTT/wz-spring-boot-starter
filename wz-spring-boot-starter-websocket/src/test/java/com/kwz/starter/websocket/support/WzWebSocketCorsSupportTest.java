package com.kwz.starter.websocket.support;

import com.kwz.starter.websocket.properties.WzWebSocketProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WzWebSocketCorsSupportTest {

    @Test
    void shouldPreferAllowedOriginPatterns() {
        WzWebSocketProperties.Endpoint endpoint = new WzWebSocketProperties.Endpoint();
        endpoint.setAllowedOriginPatterns(new String[] {"https://*.example.com"});
        endpoint.setAllowedOrigins(new String[] {"https://old.example.com"});

        assertThat(WzWebSocketCorsSupport.resolveAllowedOriginPatterns(endpoint))
                .containsExactly("https://*.example.com");
    }

    @Test
    void shouldFallbackToAllowedOrigins() {
        WzWebSocketProperties.Endpoint endpoint = new WzWebSocketProperties.Endpoint();
        endpoint.setAllowedOrigins(new String[] {"https://app.example.com"});

        assertThat(WzWebSocketCorsSupport.resolveAllowedOriginPatterns(endpoint))
                .containsExactly("https://app.example.com");
    }

    @Test
    void shouldDefaultToWildcardPattern() {
        assertThat(WzWebSocketCorsSupport.resolveAllowedOriginPatterns(new WzWebSocketProperties.Endpoint()))
                .containsExactly("*");
    }
}
