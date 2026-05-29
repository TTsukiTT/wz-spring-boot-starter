package com.kwz.starter.security.model;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * 对外暴露的当前用户信息（API 响应专用，不含 Security 内部字段）
 */
@Data
@Builder
public class UserInfo {

    private Long userId;
    private String username;
    private Set<String> roles;
    private Set<String> permissions;

    public static UserInfo from(LoginUser loginUser) {
        if (loginUser == null) {
            return null;
        }
        return UserInfo.builder()
                .userId(loginUser.getUserId())
                .username(loginUser.getUsername())
                .roles(loginUser.safeRoles())
                .permissions(loginUser.safePermissions())
                .build();
    }
}
