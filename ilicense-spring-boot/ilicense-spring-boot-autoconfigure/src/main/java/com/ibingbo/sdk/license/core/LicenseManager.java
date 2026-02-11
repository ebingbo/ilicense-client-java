package com.ibingbo.sdk.license.core;

import com.ibingbo.sdk.license.config.LicenseProperties;
import com.ibingbo.sdk.license.event.LicenseActivatedEvent;
import com.ibingbo.sdk.license.event.LicenseExpiredEvent;
import com.ibingbo.sdk.license.event.LicenseExpiringSoonEvent;
import com.ibingbo.sdk.license.exception.LicenseException;
import com.ibingbo.sdk.license.exception.LicenseExpiredException;
import com.ibingbo.sdk.license.exception.LicenseNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class LicenseManager {

    private final LicenseProperties properties;
    private final LicenseValidator validator;
    private final ApplicationEventPublisher eventPublisher;

    private volatile LicenseInfo currentLicense;

    public LicenseManager(LicenseProperties properties,
                          LicenseValidator validator,
                          ApplicationEventPublisher eventPublisher) {
        this.properties = properties;
        this.validator = validator;
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void init() {
        if (!properties.isEnabled()) {
            log.info("license validation disabled");
            return;
        }

        if (properties.isValidateOnStartup()) {
            performStartupValidation();
        }
    }

    /**
     * 启动验证
     */
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

    private void handleNoLicense() throws LicenseNotFoundException {
        log.warn("system not activated - please upload a license activation code to {}", properties.getApiPrefix() + "/activate");

        if (!properties.isAllowStartWhenExpired()) {
            throw new LicenseNotFoundException("system not activated, startup failed");
        }
    }

    private void handleExpiredLicense() throws LicenseExpiredException {
        log.error("license expired - expiry date: {}", currentLicense.getExpiryDate());

        eventPublisher.publishEvent(new LicenseExpiredEvent(currentLicense));

        if (!properties.isAllowStartWhenExpired()) {
            throw new LicenseExpiredException("license expired, startup failed: " + currentLicense.getExpiryDate());
        }
    }

    private void handleValidLicense() {
        log.info("license validation successful - customer: {}, product: {}, expiry: {}, days left: {}",
                truncate(currentLicense.getCustomerName(), 20),
                truncate(currentLicense.getProductName(), 20),
                currentLicense.getExpiryDate(),
                currentLicense.getDaysLeft());

        checkExpiryWarning();
    }

    private void checkExpiryWarning() {
        if (currentLicense.getDaysLeft() <= properties.getExpiryWarningDays()) {
            log.warn("license will expire in {} days, please renew", currentLicense.getDaysLeft());

            eventPublisher.publishEvent(new LicenseExpiringSoonEvent(
                    currentLicense,
                    currentLicense.getDaysLeft()
            ));
        }
    }

    /**
     * 定期检查License状态
     * 由 SchedulingConfigurer 定时调用
     */
    public void checkLicenseStatus() {
        if (currentLicense == null) {
            log.info("skipping check: not activated");
            return;
        }

        if (currentLicense.isExpired()) {
            log.error("periodic check: license expired");
            eventPublisher.publishEvent(new LicenseExpiredEvent(currentLicense));
        } else {
            checkExpiryWarning();
        }
    }

    /**
     * 激活License
     */
    public LicenseInfo activate(String activationCode) throws LicenseException {
        log.info("starting license activation");

        LicenseInfo license = validator.validate(activationCode);

        if (license.isExpired()) {
            throw new LicenseExpiredException("license expired: " + license.getExpiryDate());
        }

        saveLicenseToFile(activationCode);
        this.currentLicense = license;

        eventPublisher.publishEvent(new LicenseActivatedEvent(license));

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

    public void checkLicense() throws LicenseException {
        if (currentLicense == null) {
            throw new LicenseNotFoundException("system not activated");
        }
        if (currentLicense.isExpired()) {
            throw new LicenseExpiredException("license expired: " + currentLicense.getExpiryDate());
        }
    }

    public void checkModule(String moduleName) throws LicenseException {
        checkLicense();
        if (!currentLicense.hasModule(moduleName)) {
            throw new LicenseException("unauthorized module: " + moduleName);
        }
    }

    /**
     * 从文件加载License
     */
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

    /**
     * 保存License到文件
     */
    private void saveLicenseToFile(String activationCode) throws LicenseException {
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
