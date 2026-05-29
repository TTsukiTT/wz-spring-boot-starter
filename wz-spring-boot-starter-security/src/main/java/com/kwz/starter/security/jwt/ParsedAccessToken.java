package com.kwz.starter.security.jwt;

import com.kwz.starter.security.model.LoginUser;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;

/**
 * 解析后的 Access Token 元数据，供鉴权与黑名单使用
 */
@Data
@Builder
public class ParsedAccessToken {

    private String rawToken;
    private String jti;
    private Long userId;
    private Long tokenVersion;
    private Instant expiresAt;
    private LoginUser loginUser;

    public Duration remainingTtl() {
        if (expiresAt == null) {
            return Duration.ZERO;
        }
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        return ttl.isNegative() ? Duration.ZERO : ttl;
    }
}
