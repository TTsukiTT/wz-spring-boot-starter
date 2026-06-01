package com.kwz.starter.monitor.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 监控模块配置，前缀 wz.monitor.*
 */
@Data
@ConfigurationProperties(prefix = "wz.monitor")
public class WzMonitorProperties {

    /** 是否启用监控模块 */
    private boolean enabled = true;

    /** 是否注册基础健康检查 */
    private boolean healthIndicatorEnabled = true;

    /** 是否注册基础 info 信息 */
    private boolean infoContributorEnabled = true;

    /** 是否为 Micrometer 指标自动追加公共标签 */
    private boolean commonTagsEnabled = true;

    /** 追加到所有指标的公共标签 */
    private Map<String, String> commonTags = new LinkedHashMap<>();
}
