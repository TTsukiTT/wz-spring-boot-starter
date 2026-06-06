package com.kwz.starter.log;

import com.kwz.starter.log.annotation.LogHttpMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(classes = WzRequestPayloadLoggingIntegrationTest.TestApplication.class,
        properties = {
                "logging.level.com.kwz.starter.log.message=INFO",
                "logging.level.com.kwz.starter.log.access=WARN"
        })
@AutoConfigureMockMvc
class WzRequestPayloadLoggingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldLogHttpMessageWhenAnnotated(CapturedOutput output) throws Exception {
        mockMvc.perform(post("/api/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"admin\"}"))
                .andExpect(status().isOk());

        String logs = output.getOut() + output.getErr();
        assertThat(logs).contains("======== HTTP Request ========");
        assertThat(logs).contains("POST /api/echo HTTP/1.1");
        assertThat(logs).contains("Request Body:");
        assertThat(logs).contains("\"name\" : \"admin\"");
        assertThat(logs).contains("======== HTTP Response ========");
        assertThat(logs).contains("Response Body:");
        assertThat(logs).contains("\"name\" : \"admin\"");
    }

    @Test
    void shouldLogHttpMessageWhenGlobalEnabled(CapturedOutput output) throws Exception {
        // 单独启动一个带全局开关的上下文成本较高，这里验证未标注接口默认不打报文
        mockMvc.perform(post("/api/plain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":1}"))
                .andExpect(status().isOk());

        assertThat(output.getOut() + output.getErr())
                .doesNotContain("======== HTTP Request ========");
    }

    @SpringBootApplication
    static class TestApplication {

        @RestController
        static class EchoController {

            @LogHttpMessage
            @PostMapping("/api/echo")
            Map<String, String> echo(@RequestBody Map<String, String> body) {
                return body;
            }

            @PostMapping("/api/plain")
            Map<String, Integer> plain(@RequestBody Map<String, Integer> body) {
                return body;
            }
        }
    }
}
