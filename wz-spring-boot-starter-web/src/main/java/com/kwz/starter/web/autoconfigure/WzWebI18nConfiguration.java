package com.kwz.starter.web.autoconfigure;

import com.kwz.starter.core.properties.WzI18nProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * Web 层多语言配置：Locale 解析与 URL 参数切换
 */
@Configuration
@ConditionalOnProperty(prefix = "wz.i18n", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WzWebI18nConfiguration implements WebMvcConfigurer {

    private final WzI18nProperties i18nProperties;

    public WzWebI18nConfiguration(WzI18nProperties i18nProperties) {
        this.i18nProperties = i18nProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(i18nProperties.getDefaultLocale());
        return resolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public LocalValidatorFactoryBean defaultValidator(MessageSource messageSource) {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setValidationMessageSource(messageSource);
        return factory;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (StringUtils.hasText(i18nProperties.getParamName())) {
            LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
            interceptor.setParamName(i18nProperties.getParamName());
            registry.addInterceptor(interceptor);
        }
    }
}
