package com.kwz.starter.security.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwz.common.exception.BizException;
import com.kwz.common.exception.ErrorCode;
import com.kwz.common.result.Result;
import com.kwz.starter.security.exception.SecurityErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Filter 层统一 JSON 错误响应
 */
public final class SecurityFilterResponses {

    private SecurityFilterResponses() {
    }

    public static void writeUnauthorized(ObjectMapper objectMapper, HttpServletResponse response,
                                         ErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Result.fail(errorCode));
    }

    public static void writeUnauthorized(ObjectMapper objectMapper, HttpServletResponse response,
                                         BizException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                Result.fail(exception.getCode(), exception.getMessageKey(), exception.getMessage(), exception.getArgs()));
    }
}
