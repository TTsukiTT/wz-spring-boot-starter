package com.kwz.starter.log.request;

import com.kwz.starter.log.properties.WzLogProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class WzHttpMessageFormatterTest {

    @Test
    void shouldPrettyPrintJsonBody() {
        String formatted = WzHttpMessageFormatter.formatBody("{\"name\":\"admin\"}", 2048);

        assertThat(formatted).contains("\"name\" : \"admin\"");
    }

    @Test
    void shouldTruncateLongBody() {
        String formatted = WzHttpMessageFormatter.formatBody("0123456789", 5);

        assertThat(formatted).isEqualTo("01234...");
    }

    @Test
    void shouldDetectLoggableContentType() {
        assertThat(WzHttpMessageFormatter.isLoggableContentType("application/json")).isTrue();
        assertThat(WzHttpMessageFormatter.isLoggableContentType("multipart/form-data")).isFalse();
    }

    @Test
    void shouldDecodeUtf8JsonResponseWhenServletEncodingIsIso88591() {
        byte[] utf8Bytes = "用户不存在".getBytes(StandardCharsets.UTF_8);
        String decoded = new String(
                utf8Bytes,
                WzHttpMessageFormatter.resolveCharset("application/json", "ISO-8859-1"));

        assertThat(decoded).isEqualTo("用户不存在");
    }

    @Test
    void shouldSkipRequestHeadersWhenDisabled() {
        WzLogProperties properties = new WzLogProperties();
        properties.getRequest().setIncludeRequestHeaders(false);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.addHeader("X-Custom", "value");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

        String message = WzHttpMessageFormatter.format(
                request, response, cachingResponse, true, false, properties, 1);

        assertThat(message).doesNotContain("X-Custom: value");
        assertThat(message).contains("POST /api/login HTTP/1.1");
    }
}
