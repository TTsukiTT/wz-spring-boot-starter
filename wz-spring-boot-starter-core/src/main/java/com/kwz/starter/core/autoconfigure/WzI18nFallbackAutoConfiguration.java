package com.kwz.starter.core.autoconfigure;

import com.kwz.starter.core.i18n.I18nMessageResolver;
import com.kwz.starter.core.i18n.PassthroughI18nMessageResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * i18n 关闭时的兜底配置
 */
@AutoConfiguration
@ConditionalOnMissingBean(I18nMessageResolver.class)
public class WzI18nFallbackAutoConfiguration {

    @Bean
    public I18nMessageResolver i18nMessageResolver() {
        return new PassthroughI18nMessageResolver();
    }
}
