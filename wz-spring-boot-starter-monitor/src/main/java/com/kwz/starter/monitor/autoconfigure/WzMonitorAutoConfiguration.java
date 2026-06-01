package com.kwz.starter.monitor.autoconfigure;

import com.kwz.starter.monitor.health.WzMonitorHealthIndicator;
import com.kwz.starter.monitor.properties.WzMonitorProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@AutoConfiguration
@ConditionalOnProperty(prefix = "wz.monitor", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WzMonitorProperties.class)
public class WzMonitorAutoConfiguration {

    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnProperty(prefix = "wz.monitor", name = "common-tags-enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    @ConditionalOnMissingBean(name = "wzMeterRegistryCustomizer")
    public MeterRegistryCustomizer<MeterRegistry> wzMeterRegistryCustomizer(WzMonitorProperties properties,
                                                                            Environment environment) {
        return registry -> {
            Map<String, String> tags = new LinkedHashMap<>();
            String applicationName = environment.getProperty("spring.application.name");
            if (StringUtils.hasText(applicationName)) {
                tags.put("application", applicationName);
            }
            String[] activeProfiles = environment.getActiveProfiles();
            if (activeProfiles.length > 0) {
                tags.put("profile", String.join(",", activeProfiles));
            }
            tags.putAll(properties.getCommonTags());
            if (!tags.isEmpty()) {
                registry.config().meterFilter(MeterFilter.commonTags(tags.entrySet().stream()
                        .map(entry -> Tag.of(entry.getKey(), entry.getValue()))
                        .toList()));
            }
        };
    }

    @Bean
    @ConditionalOnProperty(prefix = "wz.monitor", name = "health-indicator-enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = "wzMonitorHealthIndicator")
    public WzMonitorHealthIndicator wzMonitorHealthIndicator() {
        return new WzMonitorHealthIndicator();
    }

    @Bean
    @ConditionalOnProperty(prefix = "wz.monitor", name = "info-contributor-enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = "wzMonitorInfoContributor")
    public InfoContributor wzMonitorInfoContributor() {
        return builder -> builder.withDetail("wzMonitor", Map.of(
                "enabled", true,
                "description", "Actuator + Prometheus support"
        ));
    }
}
