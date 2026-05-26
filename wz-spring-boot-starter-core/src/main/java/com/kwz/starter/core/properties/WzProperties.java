package com.kwz.starter.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 全局配置前缀: wz.*
 */
@Data
@ConfigurationProperties(prefix = "wz")
public class WzProperties {

    /** 应用标识，用于日志/链路追踪 */
    private String appName = "app";

    /** 是否开启 debug 模式 */
    private boolean debug = false;
}
