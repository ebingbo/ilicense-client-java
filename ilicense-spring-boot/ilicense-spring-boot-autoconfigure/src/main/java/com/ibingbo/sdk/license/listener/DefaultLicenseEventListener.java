package com.ibingbo.sdk.license.listener;

import com.ibingbo.sdk.license.event.LicenseActivatedEvent;
import com.ibingbo.sdk.license.event.LicenseExpiredEvent;
import com.ibingbo.sdk.license.event.LicenseExpiringSoonEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 默认License事件监听器
 * 只提供基础的日志输出
 * 可通过配置禁用
 */
@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "license",
        name = "enable-default-listener",
        havingValue = "true",
        matchIfMissing = false  // 默认不启用，让应用自己实现
)
public class DefaultLicenseEventListener {

    @EventListener
    public void onLicenseActivated(LicenseActivatedEvent event) {
        log.info("license activated successfully - customer: {}, product: {}, expiry: {}",
                event.getLicenseInfo().getCustomerName(),
                event.getLicenseInfo().getProductName(),
                event.getLicenseInfo().getExpiryDate());
    }

    @EventListener
    public void onLicenseExpired(LicenseExpiredEvent event) {
        log.error("license expired - expiry date: {}", event.getLicenseInfo().getExpiryDate());
    }

    @EventListener
    public void onLicenseExpiringSoon(LicenseExpiringSoonEvent event) {
        var info = event.getLicenseInfo();
        long daysLeft = event.getDaysLeft();

        String warningLevel;
        if (daysLeft <= 7) {
            warningLevel = "urgent";
        } else if (daysLeft <= 15) {
            warningLevel = "warning";
        } else {
            warningLevel = "notice";
        }

        log.warn("{} - license expiring soon - expiry date: {}, days left: {}, please renew in time to avoid service interruption",
                warningLevel, info.getExpiryDate(), daysLeft);
    }
}
