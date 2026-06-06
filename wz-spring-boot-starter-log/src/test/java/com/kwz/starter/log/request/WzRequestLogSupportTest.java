package com.kwz.starter.log.request;

import com.kwz.starter.log.properties.WzLogProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class WzRequestLogSupportTest {

    @Test
    void shouldMatchExcludedPath() {
        WzLogProperties properties = new WzLogProperties();

        assertThat(WzRequestLogSupport.isExcluded("/actuator/health", properties.getRequest().getExcludePatterns()))
                .isTrue();
        assertThat(WzRequestLogSupport.isExcluded("/api/users", properties.getRequest().getExcludePatterns()))
                .isFalse();
    }

    @Test
    void shouldResolveForwardedClientIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.1, 10.0.0.1");

        assertThat(WzRequestLogSupport.resolveClientIp(request)).isEqualTo("203.0.113.1");
    }

    @Test
    void shouldResolveQueryStringFromParameterMap() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ping");
        request.setParameter("q", "1");

        assertThat(WzRequestLogSupport.resolveQueryString(request)).isEqualTo("q=1");
    }
}
