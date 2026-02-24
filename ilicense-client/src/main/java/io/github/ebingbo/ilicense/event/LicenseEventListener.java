package io.github.ebingbo.ilicense.event;

import io.github.ebingbo.ilicense.core.LicenseInfo;

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
