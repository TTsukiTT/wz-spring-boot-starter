package com.kwz.starter.log.autoconfigure;

import com.kwz.starter.log.properties.WzLogProperties;
import com.kwz.starter.log.tlog.WzTLogHandlerInterceptor;
import com.kwz.starter.log.tlog.WzTLogServletFilter;
import com.yomahub.tlog.context.TLogContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 日志自动配置（含 TLog TraceId，Spring Boot 3 Jakarta 适配）
 * <p>
 * 参考：<a href="https://juejin.cn/post/7408202391504371727">SpringBoot3 适配 TLog</a>
 */
@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties(WzLogProperties.class)
public class WzLogAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "wz.log.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(name = "com.yomahub.tlog.context.TLogContext")
    InitializingBean wzTLogMdcInit() {
        return () -> TLogContext.setHasTLogMDC(true);
    }

    @Bean
    @ConditionalOnMissingBean(name = "wzTLogServletFilter")
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "wz.log.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(name = "com.yomahub.tlog.context.TLogContext")
    FilterRegistrationBean<WzTLogServletFilter> wzTLogServletFilter(WzLogProperties properties) {
        FilterRegistrationBean<WzTLogServletFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new WzTLogServletFilter(properties));
        registration.addUrlPatterns("/*");
        registration.setName("wzTLogServletFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean(WzTLogWebMvcConfigurer.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "wz.log.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(name = "com.yomahub.tlog.context.TLogContext")
    WzTLogWebMvcConfigurer wzTLogWebMvcConfigurer(WzLogProperties properties) {
        return new WzTLogWebMvcConfigurer(properties);
    }

    static final class WzTLogWebMvcConfigurer implements WebMvcConfigurer {

        private final WzLogProperties properties;

        WzTLogWebMvcConfigurer(WzLogProperties properties) {
            this.properties = properties;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new WzTLogHandlerInterceptor(properties))
                    .addPathPatterns("/**")
                    .order(Ordered.HIGHEST_PRECEDENCE);
        }
    }
}
