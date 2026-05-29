package com.kwz.starter.security.jwt;

import com.kwz.common.exception.BizException;
import com.kwz.starter.security.exception.SecurityErrorCode;
import com.kwz.starter.security.model.LoginUser;
import com.kwz.starter.security.properties.WzSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * JWT 签发与解析
 */
public class JwtService {

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISSIONS = "permissions";
    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String CLAIM_TOKEN_VERSION = "ver";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final WzSecurityProperties properties;
    private SecretKey secretKey;

    public JwtService(WzSecurityProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        String secret = properties.getJwt().getSecret();
        if (!StringUtils.hasText(secret) || secret.length() < 32) {
            throw new IllegalStateException("wz.security.jwt.secret must be at least 32 characters");
        }
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public JwtTokenPair createTokenPair(LoginUser loginUser) {
        long expireSeconds = properties.getJwt().getExpireSeconds();
        String accessToken = createToken(loginUser, TOKEN_TYPE_ACCESS, expireSeconds);
        String refreshToken = null;
        long refreshExpireSeconds = properties.getJwt().getRefreshExpireSeconds();
        if (refreshExpireSeconds > 0) {
            refreshToken = createToken(loginUser, TOKEN_TYPE_REFRESH, refreshExpireSeconds);
        }
        return new JwtTokenPair(accessToken, refreshToken, expireSeconds);
    }

    public String createAccessToken(LoginUser loginUser) {
        return createToken(loginUser, TOKEN_TYPE_ACCESS, properties.getJwt().getExpireSeconds());
    }

    public LoginUser parseAccessToken(String token) {
        return parseAccessTokenPayload(token).getLoginUser();
    }

    public ParsedAccessToken parseAccessTokenPayload(String token) {
        Claims claims = parseClaims(token);
        if (!TOKEN_TYPE_ACCESS.equals(claims.get(CLAIM_TOKEN_TYPE, String.class))) {
            throw new BizException(SecurityErrorCode.TOKEN_INVALID);
        }
        LoginUser loginUser = toLoginUser(claims);
        return ParsedAccessToken.builder()
                .rawToken(token)
                .jti(claims.getId())
                .userId(loginUser.getUserId())
                .tokenVersion(readLongClaim(claims, CLAIM_TOKEN_VERSION))
                .expiresAt(claims.getExpiration().toInstant())
                .loginUser(loginUser)
                .build();
    }

    public LoginUser parseRefreshToken(String token) {
        Claims claims = parseClaims(token);
        if (!TOKEN_TYPE_REFRESH.equals(claims.get(CLAIM_TOKEN_TYPE, String.class))) {
            throw new BizException(SecurityErrorCode.TOKEN_INVALID);
        }
        return toLoginUser(claims);
    }

    public JwtTokenPair refreshTokenPair(String refreshToken) {
        LoginUser loginUser = parseRefreshToken(refreshToken);
        return createTokenPair(loginUser);
    }

    private String createToken(LoginUser loginUser, String tokenType, long expireSeconds) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(loginUser.getUserId()))
                .claim(CLAIM_USERNAME, loginUser.getUsername())
                .claim(CLAIM_ROLES, loginUser.safeRoles())
                .claim(CLAIM_PERMISSIONS, loginUser.safePermissions())
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expireSeconds)))
                .signWith(secretKey);
        if (loginUser.getTokenVersion() != null) {
            builder.claim(CLAIM_TOKEN_VERSION, loginUser.getTokenVersion());
        }
        return builder.compact();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new BizException(SecurityErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BizException(SecurityErrorCode.TOKEN_INVALID);
        }
    }

    private LoginUser toLoginUser(Claims claims) {
        return LoginUser.builder()
                .userId(Long.valueOf(claims.getSubject()))
                .username(claims.get(CLAIM_USERNAME, String.class))
                .roles(toStringSet(claims.get(CLAIM_ROLES)))
                .permissions(toStringSet(claims.get(CLAIM_PERMISSIONS)))
                .tokenVersion(readLongClaim(claims, CLAIM_TOKEN_VERSION))
                .build();
    }

    private Long readLongClaim(Claims claims, String name) {
        Object value = claims.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    private Set<String> toStringSet(Object value) {
        if (value == null) {
            return new LinkedHashSet<>();
        }
        if (value instanceof Collection<?> collection) {
            Set<String> result = new LinkedHashSet<>();
            for (Object item : collection) {
                if (item != null) {
                    result.add(String.valueOf(item));
                }
            }
            return result;
        }
        if (value instanceof List<?> list) {
            return new LinkedHashSet<>(list.stream().map(String::valueOf).toList());
        }
        return new LinkedHashSet<>(List.of(String.valueOf(value)));
    }
}
