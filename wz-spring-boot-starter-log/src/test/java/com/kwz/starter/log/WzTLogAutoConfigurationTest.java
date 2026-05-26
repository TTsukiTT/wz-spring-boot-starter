package com.kwz.starter.log;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = WzTLogAutoConfigurationTest.TestApplication.class)
class WzTLogAutoConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldRegisterTLogServletFilter() {
        assertThat(applicationContext.getBeansOfType(FilterRegistrationBean.class))
                .containsKey("wzTLogServletFilter");
    }

    @SpringBootApplication
    static class TestApplication {
    }
}
