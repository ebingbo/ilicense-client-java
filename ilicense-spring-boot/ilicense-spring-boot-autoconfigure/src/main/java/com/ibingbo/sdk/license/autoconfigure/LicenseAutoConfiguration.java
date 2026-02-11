package com.ibingbo.sdk.license.autoconfigure;

import com.ibingbo.sdk.license.aspect.LicenseAspect;
import com.ibingbo.sdk.license.config.LicenseProperties;
import com.ibingbo.sdk.license.controller.LicenseController;
import com.ibingbo.sdk.license.core.LicenseManager;
import com.ibingbo.sdk.license.core.LicenseValidator;
import com.ibingbo.sdk.license.listener.DefaultLicenseEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

/**
 * License自动配置类
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(LicenseProperties.class)
@Import({LicenseSchedulingConfiguration.class, LicenseWebConfiguration.class})
@ConditionalOnProperty(
        prefix = "license",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class LicenseAutoConfiguration {

    /**
     * License验证器
     */
    @Bean
    @ConditionalOnMissingBean
    public LicenseValidator licenseValidator(LicenseProperties properties) {
        if (!StringUtils.hasText(properties.getPublicKey())) {
            throw new IllegalArgumentException(
                    "license public key not configure, please in application.yml configure license.public-key"
            );
        }

        log.info("license validator init ...");
        return new LicenseValidator(properties.getPublicKey());
    }

    /**
     * License管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public LicenseManager licenseManager(
            LicenseProperties properties,
            LicenseValidator validator,
            ApplicationEventPublisher eventPublisher) {

        log.info("license manager init ... storage path: {}, is validate on startup: {}, is allow start when expired: {}", properties.getStoragePath(), properties.isValidateOnStartup(), properties.isAllowStartWhenExpired());
        return new LicenseManager(properties, validator, eventPublisher);
    }

    /**
     * License AOP切面
     */
    @Bean
    @ConditionalOnClass(name = "org.aspectj.lang.ProceedingJoinPoint")
    @ConditionalOnMissingBean
    public LicenseAspect licenseAspect(LicenseManager licenseManager) {
        log.info("license aspect init ...");
        return new LicenseAspect(licenseManager);
    }

    /**
     * License REST API控制器
     * 只在Web应用中启用
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(
            prefix = "license",
            name = "enable-api",
            havingValue = "true",
            matchIfMissing = true
    )
    public LicenseController licenseController(LicenseManager licenseManager) {
        log.info("license controller init ... api prefix: {}", "${license.api-prefix:/api/license}");
        return new LicenseController(licenseManager);
    }

    /**
     * 默认License事件监听器
     * 只在配置启用时才注册
     */
    @Bean
    @ConditionalOnMissingBean(name = "defaultLicenseEventListener")
    @ConditionalOnProperty(
            prefix = "license",
            name = "enable-default-listener",
            havingValue = "true",
            matchIfMissing = false  // 默认不启用
    )
    public DefaultLicenseEventListener defaultLicenseEventListener() {
        log.info("license default event listener init ...");
        return new DefaultLicenseEventListener();
    }
}
