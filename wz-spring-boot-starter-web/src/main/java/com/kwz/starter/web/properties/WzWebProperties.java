package com.kwz.starter.web.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Web 模块配置，前缀 wz.web.*
 */
@Data
@ConfigurationProperties(prefix = "wz.web")
public class WzWebProperties {

    private WrapResult wrapResult = new WrapResult();

    @Data
    public static class WrapResult {

        /** 是否将 Controller 返回值自动包装为 Result */
        private boolean enabled = true;
    }
}
