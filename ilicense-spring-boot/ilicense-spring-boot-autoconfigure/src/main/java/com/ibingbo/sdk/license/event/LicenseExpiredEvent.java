package com.ibingbo.sdk.license.event;

import com.ibingbo.sdk.license.core.LicenseInfo;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LicenseExpiredEvent extends ApplicationEvent {
    private final LicenseInfo licenseInfo;

    public LicenseExpiredEvent(LicenseInfo licenseInfo) {
        super(licenseInfo);
        this.licenseInfo = licenseInfo;
    }
}
