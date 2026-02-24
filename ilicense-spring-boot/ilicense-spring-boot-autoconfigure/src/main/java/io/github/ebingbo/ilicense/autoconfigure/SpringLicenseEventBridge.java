package io.github.ebingbo.ilicense.autoconfigure;

import io.github.ebingbo.ilicense.core.LicenseInfo;
import io.github.ebingbo.ilicense.event.LicenseActivatedEvent;
import io.github.ebingbo.ilicense.event.LicenseEventListener;
import io.github.ebingbo.ilicense.event.LicenseExpiredEvent;
import io.github.ebingbo.ilicense.event.LicenseExpiringSoonEvent;
import org.springframework.context.ApplicationEventPublisher;

public class SpringLicenseEventBridge implements LicenseEventListener {

    private final ApplicationEventPublisher eventPublisher;

    public SpringLicenseEventBridge(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onActivated(LicenseInfo licenseInfo) {
        eventPublisher.publishEvent(new LicenseActivatedEvent(licenseInfo));
    }

    @Override
    public void onExpired(LicenseInfo licenseInfo) {
        eventPublisher.publishEvent(new LicenseExpiredEvent(licenseInfo));
    }

    @Override
    public void onExpiringSoon(LicenseInfo licenseInfo, long daysLeft) {
        eventPublisher.publishEvent(new LicenseExpiringSoonEvent(licenseInfo, daysLeft));
    }
}
