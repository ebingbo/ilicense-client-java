package io.github.ebingbo.ilicense.config;

import lombok.Data;

@Data
public class LicenseClientProperties {

    private boolean enabled = true;

    private String storagePath = System.getProperty("user.home") + "/.license/license.dat";

    private boolean validateOnStartup = false;

    private boolean allowStartWhenExpired = true;

    private int expiryWarningDays = 30;

    private String apiPrefix = "/api/license";
}
