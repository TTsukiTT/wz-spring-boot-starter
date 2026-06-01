package com.kwz.starter.monitor.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.time.Instant;

/**
 * 提供监控模块自身健康状态，便于确认 starter 已加载。
 */
public class WzMonitorHealthIndicator implements HealthIndicator {

    private final Instant startupTime = Instant.now();

    @Override
    public Health health() {
        return Health.up()
                .withDetail("component", "wz-monitor")
                .withDetail("startupTime", startupTime.toString())
                .build();
    }
}
