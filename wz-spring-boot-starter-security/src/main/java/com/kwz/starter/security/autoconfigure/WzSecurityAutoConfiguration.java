package com.kwz.starter.security.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwz.starter.security.aspect.AuthorizationAspect;
import com.kwz.starter.security.filter.JwtAuthenticationFilter;
import com.kwz.starter.security.handler.WzAccessDeniedHandler;
import com.kwz.starter.security.handler.WzAuthenticationEntryPoint;
import com.kwz.starter.security.jwt.JwtService;
import com.kwz.starter.security.properties.WzSecurityProperties;
import com.kwz.starter.security.spi.TokenBlacklistService;
import com.kwz.starter.security.support.NoopTokenBlacklistService;
import com.kwz.starter.security.support.SecurityWhitelistResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "wz.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WzSecurityProperties.class)
@EnableMethodSecurity
public class WzSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtService jwtService(WzSecurityProperties properties) {
        return new JwtService(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenBlacklistService tokenBlacklistService() {
        return new NoopTokenBlacklistService();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService,
                                                           WzSecurityProperties properties,
                                                           TokenBlacklistService tokenBlacklistService,
                                                           ObjectProvider<ObjectMapper> objectMapperProvider) {
        return new JwtAuthenticationFilter(jwtService, properties, tokenBlacklistService,
                objectMapperProvider.getIfAvailable(ObjectMapper::new));
    }

    @Bean
    @ConditionalOnMissingBean
    public WzAuthenticationEntryPoint wzAuthenticationEntryPoint(ObjectProvider<ObjectMapper> objectMapperProvider) {
        return new WzAuthenticationEntryPoint(objectMapperProvider.getIfAvailable(ObjectMapper::new));
    }

    @Bean
    @ConditionalOnMissingBean
    public WzAccessDeniedHandler wzAccessDeniedHandler(ObjectProvider<ObjectMapper> objectMapperProvider) {
        return new WzAccessDeniedHandler(objectMapperProvider.getIfAvailable(ObjectMapper::new));
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationAspect authorizationAspect() {
        return new AuthorizationAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain wzSecurityFilterChain(HttpSecurity http,
                                                     JwtAuthenticationFilter jwtAuthenticationFilter,
                                                     WzAuthenticationEntryPoint authenticationEntryPoint,
                                                     WzAccessDeniedHandler accessDeniedHandler,
                                                     WzSecurityProperties properties,
                                                     ApplicationContext applicationContext)
            throws Exception {
        String[] whitelist = SecurityWhitelistResolver.resolve(properties, applicationContext);
        http.csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(whitelist).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
