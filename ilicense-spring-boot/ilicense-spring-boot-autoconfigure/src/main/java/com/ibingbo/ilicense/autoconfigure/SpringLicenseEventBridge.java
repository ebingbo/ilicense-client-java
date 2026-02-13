package com.ibingbo.ilicense.autoconfigure;

import com.ibingbo.ilicense.core.LicenseInfo;
import com.ibingbo.ilicense.event.LicenseActivatedEvent;
import com.ibingbo.ilicense.event.LicenseEventListener;
import com.ibingbo.ilicense.event.LicenseExpiredEvent;
import com.ibingbo.ilicense.event.LicenseExpiringSoonEvent;
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
