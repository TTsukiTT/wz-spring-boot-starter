package com.kwz.starter.log.autoconfigure;

import com.kwz.starter.log.properties.WzLogProperties;
import com.kwz.starter.log.support.WzLogPatterns;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 在 Logback 初始化前，将 wz.log.* 映射为 Spring Boot 原生 logging.* 配置
 */
public class WzLogEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        WzLogProperties properties = Binder.get(environment)
                .bind("wz.log", Bindable.of(WzLogProperties.class))
                .orElseGet(WzLogProperties::new);

        resolveLogFileName(environment, properties);

        Map<String, Object> loggingConfig = new HashMap<>();

        if (!properties.getConsole().isEnabled() && properties.getFile().isEnabled()) {
            applyFileOnlyLogback(loggingConfig, environment);
        }

        if (properties.getConsole().isEnabled()) {
            applyConsolePattern(loggingConfig, environment, properties);
        }

        if (properties.getFile().isEnabled()) {
            applyFilePattern(loggingConfig, environment, properties);
            applyFileConfig(loggingConfig, properties);
        }

        if (!loggingConfig.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("wzLoggingConfig", loggingConfig));
        }
    }

    private void applyFileOnlyLogback(Map<String, Object> loggingConfig, ConfigurableEnvironment environment) {
        if (!environment.containsProperty("logging.config")) {
            loggingConfig.put("logging.config", "classpath:wz-logback-file-only.xml");
        }
    }

    private void applyConsolePattern(Map<String, Object> loggingConfig, ConfigurableEnvironment environment,
                                     WzLogProperties properties) {
        if (!environment.containsProperty("logging.pattern.console")) {
            String consolePattern = StringUtils.hasText(properties.getPattern().getConsole())
                    ? properties.getPattern().getConsole()
                    : WzLogPatterns.CONSOLE;
            loggingConfig.put("logging.pattern.console", consolePattern);
        }
    }

    private void applyFilePattern(Map<String, Object> loggingConfig, ConfigurableEnvironment environment,
                                  WzLogProperties properties) {
        if (!environment.containsProperty("logging.pattern.file")) {
            String filePattern = StringUtils.hasText(properties.getPattern().getFile())
                    ? properties.getPattern().getFile()
                    : WzLogPatterns.FILE;
            loggingConfig.put("logging.pattern.file", filePattern);
        }
    }

    private void applyFileConfig(Map<String, Object> loggingConfig, WzLogProperties properties) {
        loggingConfig.put("logging.file.name", buildLogFilePath(properties));
        loggingConfig.put("logging.logback.rollingpolicy.max-file-size", properties.getFile().getMaxFileSize());
        loggingConfig.put("logging.logback.rollingpolicy.max-history", properties.getFile().getMaxHistory());
        loggingConfig.put("logging.logback.rollingpolicy.total-size-cap", properties.getFile().getTotalSizeCap());
        loggingConfig.put("logging.logback.rollingpolicy.clean-history-on-start",
                properties.getFile().isCleanHistoryOnStart());
        loggingConfig.put("logging.logback.rollingpolicy.file-name-pattern", buildRollingPattern(properties));
    }

    private void resolveLogFileName(ConfigurableEnvironment environment, WzLogProperties properties) {
        if (!environment.containsProperty("wz.log.file.name")) {
            String appName = environment.getProperty("spring.application.name");
            if (StringUtils.hasText(appName)) {
                properties.getFile().setName(appName);
            }
        }
    }

    private String buildLogFilePath(WzLogProperties properties) {
        String path = properties.getFile().getPath();
        String name = properties.getFile().getName();
        if (path.endsWith("/") || path.endsWith("\\")) {
            return path + name + ".log";
        }
        return path + "/" + name + ".log";
    }

    private String buildRollingPattern(WzLogProperties properties) {
        if (properties.getFile().isCompress()) {
            return "${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz";
        }
        return "${LOG_FILE}.%d{yyyy-MM-dd}.%i";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
