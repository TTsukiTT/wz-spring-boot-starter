package com.kwz.starter.security.jwt;

import com.kwz.starter.security.model.LoginUser;
import com.kwz.starter.security.properties.WzSecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        WzSecurityProperties properties = new WzSecurityProperties();
        properties.getJwt().setSecret("test-secret-key-at-least-32-characters-long");
        properties.getJwt().setExpireSeconds(3600);
        properties.getJwt().setRefreshExpireSeconds(86400);
        jwtService = new JwtService(properties);
        jwtService.init();
    }

    @Test
    void shouldCreateAndParseAccessToken() {
        LoginUser loginUser = LoginUser.builder()
                .userId(100L)
                .username("demo")
                .roles(Set.of("admin"))
                .permissions(Set.of("user:read", "user:delete"))
                .build();

        String token = jwtService.createAccessToken(loginUser);
        LoginUser parsed = jwtService.parseAccessToken(token);

        assertThat(parsed.getUserId()).isEqualTo(100L);
        assertThat(parsed.getUsername()).isEqualTo("demo");
        assertThat(parsed.safeRoles()).containsExactly("admin");
        assertThat(parsed.safePermissions()).containsExactlyInAnyOrder("user:read", "user:delete");
    }

    @Test
    void shouldIncludeJtiAndTokenVersion() {
        LoginUser loginUser = LoginUser.builder()
                .userId(100L)
                .username("demo")
                .tokenVersion(3L)
                .build();

        ParsedAccessToken payload = jwtService.parseAccessTokenPayload(jwtService.createAccessToken(loginUser));

        assertThat(payload.getJti()).isNotBlank();
        assertThat(payload.getTokenVersion()).isEqualTo(3L);
        assertThat(payload.getUserId()).isEqualTo(100L);
        assertThat(payload.remainingTtl().toSeconds()).isPositive();
    }

    @Test
    void shouldRefreshTokenPair() {
        LoginUser loginUser = LoginUser.builder()
                .userId(200L)
                .username("refresh-user")
                .roles(Set.of("user"))
                .permissions(Set.of("order:read"))
                .build();

        JwtTokenPair tokenPair = jwtService.createTokenPair(loginUser);
        JwtTokenPair refreshed = jwtService.refreshTokenPair(tokenPair.getRefreshToken());

        assertThat(refreshed.getAccessToken()).isNotBlank();
        assertThat(jwtService.parseAccessToken(refreshed.getAccessToken()).getUserId()).isEqualTo(200L);
    }
}
