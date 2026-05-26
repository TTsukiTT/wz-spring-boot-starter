package com.kwz.starter.core.autoconfigure;

import com.kwz.starter.core.properties.WzProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 核心自动配置入口
 */
@AutoConfiguration
@EnableConfigurationProperties(WzProperties.class)
public class WzCoreAutoConfiguration {
}
