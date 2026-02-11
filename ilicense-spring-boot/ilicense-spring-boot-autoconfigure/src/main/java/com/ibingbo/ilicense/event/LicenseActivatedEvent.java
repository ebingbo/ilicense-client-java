package com.ibingbo.ilicense.event;

import com.ibingbo.ilicense.core.LicenseInfo;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LicenseActivatedEvent extends ApplicationEvent {
    private final LicenseInfo licenseInfo;
    public LicenseActivatedEvent(LicenseInfo licenseInfo) {
        super(licenseInfo);
        this.licenseInfo = licenseInfo;
    }
}
