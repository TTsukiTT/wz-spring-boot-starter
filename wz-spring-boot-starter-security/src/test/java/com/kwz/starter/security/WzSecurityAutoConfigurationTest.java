package com.kwz.starter.security;

import com.kwz.starter.security.aspect.AuthorizationAspect;
import com.kwz.starter.security.jwt.JwtService;
import com.kwz.starter.security.spi.TokenBlacklistService;
import com.kwz.starter.security.support.NoopTokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = WzSecurityAutoConfigurationTest.TestApplication.class)
class WzSecurityAutoConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldRegisterSecurityBeans() {
        assertThat(applicationContext.getBean(JwtService.class)).isNotNull();
        assertThat(applicationContext.getBean(SecurityFilterChain.class)).isNotNull();
        assertThat(applicationContext.getBean(AuthorizationAspect.class)).isNotNull();
        assertThat(applicationContext.getBean(TokenBlacklistService.class)).isInstanceOf(NoopTokenBlacklistService.class);
    }

    @SpringBootApplication
    static class TestApplication {
    }
}
