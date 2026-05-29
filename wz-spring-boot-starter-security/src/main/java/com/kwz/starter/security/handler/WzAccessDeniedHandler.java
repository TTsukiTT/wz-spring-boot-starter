package com.kwz.starter.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwz.common.exception.GlobalErrorCode;
import com.kwz.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 无权限时返回统一 JSON 响应
 */
public class WzAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public WzAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Result.fail(GlobalErrorCode.FORBIDDEN));
    }
}
