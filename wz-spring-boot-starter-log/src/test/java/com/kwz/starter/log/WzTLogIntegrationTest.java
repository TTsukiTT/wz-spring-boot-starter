package com.kwz.starter.log;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest(classes = WzTLogIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
class WzTLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnTlogTraceIdHeader() throws Exception {
        String traceId = mockMvc.perform(get("/test-trace"))
                .andExpect(header().exists("tlogTraceId"))
                .andReturn()
                .getResponse()
                .getHeader("tlogTraceId");
        assertThat(traceId).isNotBlank();
    }

    @SpringBootApplication
    static class TestApplication {

        @RestController
        static class TraceTestController {

            @GetMapping("/test-trace")
            String trace() {
                return MDC.get("traceId") + "|" + MDC.get("tlogTraceId");
            }
        }
    }
}
