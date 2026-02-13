package com.ibingbo.ilicense.event;

import com.ibingbo.ilicense.core.LicenseInfo;

public interface LicenseEventListener {

    LicenseEventListener NO_OP = new LicenseEventListener() {
    };

    default void onActivated(LicenseInfo licenseInfo) {
    }

    default void onExpired(LicenseInfo licenseInfo) {
    }

    default void onExpiringSoon(LicenseInfo licenseInfo, long daysLeft) {
    }
}
