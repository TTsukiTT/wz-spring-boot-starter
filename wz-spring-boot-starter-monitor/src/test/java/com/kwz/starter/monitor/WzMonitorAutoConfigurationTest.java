package com.kwz.starter.monitor;

import com.kwz.starter.monitor.autoconfigure.WzMonitorAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class WzMonitorAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WzMonitorAutoConfiguration.class))
            .withPropertyValues("spring.application.name=monitor-test");

    @Test
    void shouldRegisterMonitorBeansByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("wzMonitorHealthIndicator");
            assertThat(context).hasBean("wzMonitorInfoContributor");
            assertThat(context).hasBean("wzMeterRegistryCustomizer");
            assertThat(context.getBean(InfoContributor.class)).isNotNull();
        });
    }

    @Test
    void shouldDisableAllBeansWhenMonitorDisabled() {
        contextRunner
                .withPropertyValues("wz.monitor.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean("wzMonitorHealthIndicator");
                    assertThat(context).doesNotHaveBean("wzMonitorInfoContributor");
                    assertThat(context).doesNotHaveBean("wzMeterRegistryCustomizer");
                });
    }
}
