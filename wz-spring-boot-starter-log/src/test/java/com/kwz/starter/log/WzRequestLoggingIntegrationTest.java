package com.kwz.starter.log;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(classes = WzRequestLoggingIntegrationTest.TestApplication.class,
        properties = "logging.level.com.kwz.starter.log.access=INFO")
@AutoConfigureMockMvc
class WzRequestLoggingIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRegisterRequestLoggingFilter() {
        assertThat(applicationContext.getBeansOfType(FilterRegistrationBean.class))
                .containsKey("wzRequestLoggingFilter");
    }

    @Test
    void shouldWriteAccessLog(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/api/ping").param("q", "1"))
                .andExpect(status().isOk());

        assertThat(output.getOut() + output.getErr())
                .contains("GET /api/ping?q=1 status=200")
                .contains("client=127.0.0.1");
    }

    @Test
    void shouldNotLogExcludedActuatorPath(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        assertThat(output.getOut() + output.getErr())
                .doesNotContain("GET /actuator/health status=");
    }

    @SpringBootApplication
    static class TestApplication {

        @RestController
        static class PingController {

            @GetMapping("/api/ping")
            String ping() {
                return "pong";
            }
        }

        @RestController
        static class ActuatorStubController {

            @GetMapping("/actuator/health")
            String health() {
                return "UP";
            }
        }
    }
}
