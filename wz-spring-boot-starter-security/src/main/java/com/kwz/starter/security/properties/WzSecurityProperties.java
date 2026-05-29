package com.kwz.starter.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Security 模块配置，前缀 wz.security.*
 */
@Data
@ConfigurationProperties(prefix = "wz.security")
public class WzSecurityProperties {

    /** 是否启用 Security 模块 */
    private boolean enabled = true;

    private Jwt jwt = new Jwt();

    /** 免认证路径（Ant 风格） */
    private List<String> whitelist = new ArrayList<>(List.of(
            "/api/auth/**",
            "/actuator/**",
            "/error"
    ));

    /** 是否扫描 {@link com.kwz.starter.security.annotation.PermitAll} 注解 */
    private boolean annotationWhitelistEnabled = true;

    @Data
    public static class Jwt {

        /** HS256 密钥，长度至少 32 字符 */
        private String secret;

        /** Access Token 有效期（秒） */
        private long expireSeconds = 7200;

        /** Refresh Token 有效期（秒），0 表示不启用刷新 Token */
        private long refreshExpireSeconds = 604800;

        /** 请求头名称 */
        private String header = "Authorization";

        /** Token 前缀 */
        private String prefix = "Bearer ";
    }
}
