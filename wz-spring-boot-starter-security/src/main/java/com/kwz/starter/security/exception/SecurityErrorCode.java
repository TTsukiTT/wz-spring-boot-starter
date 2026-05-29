package com.kwz.starter.security.exception;

import com.kwz.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Security 模块错误码
 */
@Getter
@RequiredArgsConstructor
public enum SecurityErrorCode implements ErrorCode {

    TOKEN_MISSING(40101, "未提供认证 Token"),
    TOKEN_INVALID(40102, "Token 无效"),
    TOKEN_EXPIRED(40103, "Token 已过期"),
    TOKEN_REVOKED(40104, "Token 已失效"),
    PERMISSION_DENIED(40301, "权限不足"),
    ROLE_DENIED(40302, "角色不足");

    private final int code;
    private final String message;
}
