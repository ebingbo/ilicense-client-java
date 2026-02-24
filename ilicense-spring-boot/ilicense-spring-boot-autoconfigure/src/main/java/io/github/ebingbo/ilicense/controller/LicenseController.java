package io.github.ebingbo.ilicense.controller;

import io.github.ebingbo.ilicense.core.LicenseInfo;
import io.github.ebingbo.ilicense.core.LicenseManager;
import io.github.ebingbo.ilicense.exception.LicenseException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.*;

/**
 * License REST API
 * 使用Spring Boot 3.x的新特性
 */
@RestController
@RequestMapping("${license.api-prefix:/api/license}")
@RequiredArgsConstructor
@ConditionalOnWebApplication
public class LicenseController {

    private final LicenseManager licenseManager;

    /**
     * 激活License
     */
    @PostMapping("/activate")
    public Result<LicenseInfo> activate(
            @RequestBody ActivateRequest request) {
        try {
            LicenseInfo info = licenseManager.activate(request.getActivationCode());
            return Result.success(info, "activation successful");
        } catch (LicenseException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取License信息
     */
    @GetMapping("/info")
    public Result<LicenseInfo> getInfo() {
        LicenseInfo info = licenseManager.getCurrentLicense();
        if (info == null) {
            return Result.error("system not activated");
        }
        return Result.success(info);
    }

    /**
     * 检查License状态
     */
    @GetMapping("/status")
    public Result<LicenseStatus> getStatus() {
        LicenseInfo info = licenseManager.getCurrentLicense();

        if (info == null) {
            LicenseStatus status = new LicenseStatus(
                    false, "not activated", null, 0
            );
            return Result.success(status);
        }

        boolean valid = !info.isExpired();
        String statusText = valid ? "valid" : "expired";

        LicenseStatus status = new LicenseStatus(
                valid, statusText, info.getExpireAt(), info.getDaysLeft()
        );

        return Result.success(status);
    }

    /**
     * 检查模块权限
     */
    @GetMapping("/check-module")
    public Result<ModuleCheckResult> checkModule(
            @RequestParam String moduleName) {

        boolean hasModule = licenseManager.hasModule(moduleName);
        String message = hasModule ? "authorized" : "unauthorized";

        ModuleCheckResult result = new ModuleCheckResult(
                moduleName, hasModule, message
        );

        return Result.success(result);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<HealthStatus> health() {
        boolean valid = licenseManager.isValid();
        LicenseInfo info = licenseManager.getCurrentLicense();

        HealthStatus health = new HealthStatus();
        health.setLicenseValid(valid);
        health.setActivated(info != null);

        if (info != null) {
            health.setDaysLeft(info.getDaysLeft());
            health.setExpiryDate(info.getExpireAt());
        }

        return Result.success(health);
    }

    // DTO类
    @Data
    public static class ActivateRequest {
        private String activationCode;
    }

    @Data
    @RequiredArgsConstructor
    public static class LicenseStatus {
        private final boolean valid;
        private final String status;
        private final Object expiryDate;
        private final long daysLeft;
    }

    @Data
    @RequiredArgsConstructor
    public static class ModuleCheckResult {
        private final String moduleName;
        private final boolean authorized;
        private final String message;
    }

    @Data
    public static class HealthStatus {
        private boolean activated;
        private boolean licenseValid;
        private long daysLeft;
        private Object expiryDate;
    }

    @Data
    @RequiredArgsConstructor
    public static class Result<T> {
        private final int code;
        private final String message;
        private final T data;

        public static <T> Result<T> success(T data) {
            return new Result<>(0, "ok", data);
        }

        public static <T> Result<T> success(T data, String message) {
            return new Result<>(0, message, data);
        }

        public static <T> Result<T> error(String message) {
            return new Result<>(-1, message, null);
        }
    }
}
