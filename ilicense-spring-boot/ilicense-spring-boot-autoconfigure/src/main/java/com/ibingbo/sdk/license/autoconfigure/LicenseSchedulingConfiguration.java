package com.ibingbo.sdk.license.autoconfigure;

import com.ibingbo.sdk.license.config.LicenseProperties;
import com.ibingbo.sdk.license.core.LicenseManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "license",
        name = "enable-scheduled-check",
        havingValue = "true",
        matchIfMissing = true
)
public class LicenseSchedulingConfiguration implements SchedulingConfigurer {
    private final LicenseManager licenseManager;
    private final LicenseProperties properties;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        Duration interval = properties.getCheckInterval();
        long intervalMillis = interval.toMillis();

        log.info("license scheduled check configured - interval: {} ({} hours), milliseconds: {}",
                formatDuration(interval), interval.toHours(), intervalMillis);

        // 注册定时任务
        taskRegistrar.addFixedDelayTask(
                () -> {
                    try {
                        licenseManager.checkLicenseStatus();
                    } catch (Exception e) {
                        log.error("license scheduled check fail", e);
                    }
                },
                intervalMillis
        );
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        if (hours > 0) {
            return String.format("%dh", hours);
        } else if (minutes > 0) {
            return String.format("%dm", minutes);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
