package com.ibingbo.ilicense.core;

import com.ibingbo.ilicense.config.LicenseClientProperties;
import com.ibingbo.ilicense.event.LicenseEventListener;
import com.ibingbo.ilicense.exception.LicenseException;
import com.ibingbo.ilicense.exception.LicenseExpiredException;
import com.ibingbo.ilicense.exception.LicenseNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LicenseManager {

    private static final Logger log = LoggerFactory.getLogger(LicenseManager.class);

    private final LicenseClientProperties properties;
    private final LicenseValidator validator;
    private final LicenseEventListener eventListener;

    private volatile LicenseInfo currentLicense;

    public LicenseManager(LicenseClientProperties properties,
                          LicenseValidator validator,
                          LicenseEventListener eventListener) {
        this.properties = properties;
        this.validator = validator;
        this.eventListener = eventListener == null ? LicenseEventListener.NO_OP : eventListener;
    }

    public void init() {
        if (!properties.isEnabled()) {
            log.info("license validation disabled");
            return;
        }

        if (properties.isValidateOnStartup()) {
            performStartupValidation();
        }
    }

    private void performStartupValidation() {
        try {
            loadLicenseFromFile();

            if (currentLicense == null) {
                handleNoLicense();
            } else if (currentLicense.isExpired()) {
                handleExpiredLicense();
            } else {
                handleValidLicense();
            }

        } catch (Exception e) {
            log.error("license initialization failed", e);
            if (!properties.isAllowStartWhenExpired()) {
                throw new RuntimeException("license initialization failed", e);
            }
        }
    }

    private void handleNoLicense() {
        log.warn("system not activated - please upload a license activation code to {}",
                properties.getApiPrefix() + "/activate");

        if (!properties.isAllowStartWhenExpired()) {
            throw new LicenseNotFoundException("system not activated, startup failed");
        }
    }

    private void handleExpiredLicense() {
        log.error("license expired - expiry date: {}", currentLicense.getExpireAt());

        eventListener.onExpired(currentLicense);

        if (!properties.isAllowStartWhenExpired()) {
            throw new LicenseExpiredException("license expired, startup failed: " + currentLicense.getExpireAt());
        }
    }

    private void handleValidLicense() {
        log.info("license validation successful - customer: {}, product: {}, expiry: {}, days left: {}",
                truncate(currentLicense.getCustomerName(), 20),
                truncate(currentLicense.getProductName(), 20),
                currentLicense.getExpireAt(),
                currentLicense.getDaysLeft());

        checkExpiryWarning();
    }

    private void checkExpiryWarning() {
        if (currentLicense.getDaysLeft() <= properties.getExpiryWarningDays()) {
            log.warn("license will expire in {} days, please renew", currentLicense.getDaysLeft());
            eventListener.onExpiringSoon(currentLicense, currentLicense.getDaysLeft());
        }
    }

    public void checkLicenseStatus() {
        if (currentLicense == null) {
            log.info("skipping check: not activated");
            return;
        }

        if (currentLicense.isExpired()) {
            log.error("periodic check: license expired");
            eventListener.onExpired(currentLicense);
        } else {
            checkExpiryWarning();
        }
    }

    public LicenseInfo activate(String activationCode) {
        log.info("starting license activation");

        LicenseInfo license = validator.validate(activationCode);

        if (license.isExpired()) {
            throw new LicenseExpiredException("license expired: " + license.getExpireAt());
        }

        saveLicenseToFile(activationCode);
        this.currentLicense = license;

        eventListener.onActivated(license);

        log.info("license activated successfully: {}", license.getCustomerName());

        return license;
    }

    public LicenseInfo getCurrentLicense() {
        return currentLicense;
    }

    public boolean isValid() {
        return currentLicense != null && !currentLicense.isExpired();
    }

    public boolean hasModule(String moduleName) {
        return currentLicense != null && currentLicense.hasModule(moduleName);
    }

    public void checkLicense() {
        if (currentLicense == null) {
            throw new LicenseNotFoundException("system not activated");
        }
        if (currentLicense.isExpired()) {
            throw new LicenseExpiredException("license expired: " + currentLicense.getExpireAt());
        }
    }

    public void checkModule(String moduleName) {
        checkLicense();
        if (!currentLicense.hasModule(moduleName)) {
            throw new LicenseException("unauthorized module: " + moduleName);
        }
    }

    private void loadLicenseFromFile() {
        try {
            if (!Files.exists(Paths.get(properties.getStoragePath()))) {
                log.info("license file does not exist: {}", properties.getStoragePath());
                return;
            }

            String activationCode = Files.readString(
                    Paths.get(properties.getStoragePath()),
                    StandardCharsets.UTF_8
            );

            this.currentLicense = validator.validate(activationCode);
            log.info("license loaded successfully from file");

        } catch (Exception e) {
            log.error("failed to load license file", e);
        }
    }

    private void saveLicenseToFile(String activationCode) {
        try {
            Path dir = Paths.get(properties.getStoragePath()).getParent();
            if (dir != null && !Files.exists(dir)) {
                Files.createDirectories(Paths.get(properties.getStoragePath()).getParent());
            }
            Files.writeString(
                    Paths.get(properties.getStoragePath()),
                    activationCode,
                    StandardCharsets.UTF_8
            );
            log.info("license saved: {}", properties.getStoragePath());
        } catch (Exception e) {
            throw new LicenseException("failed to save license", e);
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
