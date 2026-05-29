package com.kwz.starter.security.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserInfoTest {

    @Test
    void shouldConvertFromLoginUser() {
        LoginUser loginUser = LoginUser.builder()
                .userId(1L)
                .username("demo")
                .roles(Set.of("admin"))
                .permissions(Set.of("user:read"))
                .tokenVersion(2L)
                .build();

        UserInfo userInfo = UserInfo.from(loginUser);

        assertThat(userInfo.getUserId()).isEqualTo(1L);
        assertThat(userInfo.getUsername()).isEqualTo("demo");
        assertThat(userInfo.getRoles()).containsExactly("admin");
        assertThat(userInfo.getPermissions()).containsExactly("user:read");
    }
}
