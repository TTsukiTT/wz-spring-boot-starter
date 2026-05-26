package com.kwz.starter.core.autoconfigure;

import com.kwz.starter.core.i18n.DefaultI18nMessageResolver;
import com.kwz.starter.core.i18n.I18nMessageResolver;
import com.kwz.starter.core.properties.WzI18nProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * 多语言自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(WzI18nProperties.class)
@ConditionalOnProperty(prefix = "wz.i18n", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WzI18nAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MessageSource messageSource(WzI18nProperties properties) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames(properties.getBasenames().toArray(new String[0]));
        messageSource.setDefaultEncoding(properties.getEncoding());
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(properties.isUseCodeAsDefaultMessage());
        return messageSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public I18nMessageResolver i18nMessageResolver(MessageSource messageSource) {
        return new DefaultI18nMessageResolver(messageSource);
    }
}
