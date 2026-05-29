package com.kwz.starter.security.jwt;

import com.kwz.starter.security.model.LoginUser;
import com.kwz.starter.security.properties.WzSecurityProperties;
import com.kwz.starter.security.spi.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBlacklistServiceTest {

    private JwtService jwtService;
    private InMemoryTokenBlacklistService blacklistService;

    @BeforeEach
    void setUp() {
        WzSecurityProperties properties = new WzSecurityProperties();
        properties.getJwt().setSecret("test-secret-key-at-least-32-characters-long");
        jwtService = new JwtService(properties);
        jwtService.init();
        blacklistService = new InMemoryTokenBlacklistService();
    }

    @Test
    void shouldBlockAccessTokenByJti() {
        LoginUser loginUser = LoginUser.builder()
                .userId(1L)
                .username("demo")
                .roles(Set.of("user"))
                .permissions(Set.of("user:read"))
                .tokenVersion(1L)
                .build();
        String token = jwtService.createAccessToken(loginUser);
        ParsedAccessToken payload = jwtService.parseAccessTokenPayload(token);

        assertThat(blacklistService.isBlocked(payload)).isFalse();

        blacklistService.blockAccessToken(payload);

        assertThat(blacklistService.isBlocked(payload)).isTrue();
    }

    @Test
    void shouldInvalidateUserByTokenVersion() {
        LoginUser loginUser = LoginUser.builder()
                .userId(2L)
                .username("demo")
                .tokenVersion(1L)
                .build();
        ParsedAccessToken payload = jwtService.parseAccessTokenPayload(jwtService.createAccessToken(loginUser));

        assertThat(blacklistService.isBlocked(payload)).isFalse();

        blacklistService.invalidateUser(2L);

        assertThat(blacklistService.isBlocked(payload)).isTrue();
    }

    /**
     * 参考实现：内存版黑名单，演示 jti 拉黑 + 用户版本号失效
     */
    static class InMemoryTokenBlacklistService implements TokenBlacklistService {

        private final Set<String> blockedJtis = ConcurrentHashMap.newKeySet();
        private final ConcurrentHashMap<Long, Long> userVersions = new ConcurrentHashMap<>();

        @Override
        public boolean isBlocked(ParsedAccessToken accessToken) {
            if (accessToken.getJti() != null && blockedJtis.contains(accessToken.getJti())) {
                return true;
            }
            Long currentVersion = userVersions.get(accessToken.getUserId());
            return currentVersion != null
                    && accessToken.getTokenVersion() != null
                    && accessToken.getTokenVersion() < currentVersion;
        }

        @Override
        public void blockAccessToken(ParsedAccessToken accessToken) {
            if (accessToken.getJti() != null) {
                blockedJtis.add(accessToken.getJti());
            }
        }

        @Override
        public void invalidateUser(Long userId) {
            userVersions.compute(userId, (id, version) -> version == null ? 2L : version + 1);
        }
    }
}
