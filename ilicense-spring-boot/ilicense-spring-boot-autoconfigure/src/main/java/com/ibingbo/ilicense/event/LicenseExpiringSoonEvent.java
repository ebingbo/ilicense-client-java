package com.ibingbo.ilicense.event;

import com.ibingbo.ilicense.core.LicenseInfo;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LicenseExpiringSoonEvent extends ApplicationEvent {
    private final LicenseInfo licenseInfo;
    private final long daysLeft;

    public LicenseExpiringSoonEvent(LicenseInfo licenseInfo, long daysLeft) {
        super(licenseInfo);
        this.licenseInfo = licenseInfo;
        this.daysLeft = daysLeft;
    }
}
