package com.kwz.starter.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录成功后返回的 Token 对
 */
@Data
@AllArgsConstructor
public class JwtTokenPair {

    private String accessToken;
    private String refreshToken;
    private long expireSeconds;
}
