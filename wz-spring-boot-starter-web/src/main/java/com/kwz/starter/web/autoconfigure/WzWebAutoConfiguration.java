package com.kwz.starter.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwz.starter.core.i18n.I18nMessageResolver;
import com.kwz.starter.log.autoconfigure.WzLogAutoConfiguration;
import com.kwz.starter.web.advice.ResultI18nResponseBodyAdvice;
import com.kwz.starter.web.advice.ResultStringResponseBodyAdvice;
import com.kwz.starter.web.advice.ResultWrapperResponseBodyAdvice;
import com.kwz.starter.web.handler.GlobalExceptionHandler;
import com.kwz.starter.web.properties.WzWebProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(WzWebProperties.class)
@Import({WzWebI18nConfiguration.class, WzLogAutoConfiguration.class})
public class WzWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "wz.web.wrap-result", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ResultWrapperResponseBodyAdvice resultWrapperResponseBodyAdvice(WzWebProperties webProperties) {
        return new ResultWrapperResponseBodyAdvice(webProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "wz.web.wrap-result", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ResultStringResponseBodyAdvice resultStringResponseBodyAdvice(ObjectMapper objectMapper,
                                                                         WzWebProperties webProperties) {
        return new ResultStringResponseBodyAdvice(objectMapper, webProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "wz.i18n", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ResultI18nResponseBodyAdvice resultI18nResponseBodyAdvice(I18nMessageResolver i18nMessageResolver) {
        return new ResultI18nResponseBodyAdvice(i18nMessageResolver);
    }
}
