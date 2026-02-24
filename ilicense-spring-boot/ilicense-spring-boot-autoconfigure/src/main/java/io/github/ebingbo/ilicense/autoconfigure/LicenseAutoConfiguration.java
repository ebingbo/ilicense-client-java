package io.github.ebingbo.ilicense.autoconfigure;

import io.github.ebingbo.ilicense.aspect.LicenseAspect;
import io.github.ebingbo.ilicense.config.LicenseClientProperties;
import io.github.ebingbo.ilicense.config.LicenseProperties;
import io.github.ebingbo.ilicense.controller.LicenseController;
import io.github.ebingbo.ilicense.core.LicenseManager;
import io.github.ebingbo.ilicense.core.LicenseValidator;
import io.github.ebingbo.ilicense.event.LicenseEventListener;
import io.github.ebingbo.ilicense.listener.DefaultLicenseEventListener;
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

    @Bean
    @ConditionalOnMissingBean
    public LicenseEventListener licenseEventListener(ApplicationEventPublisher eventPublisher) {
        return new SpringLicenseEventBridge(eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public LicenseManager licenseManager(
            LicenseProperties properties,
            LicenseValidator validator,
            LicenseEventListener eventListener) {

        log.info("license manager init ... storage path: {}, is validate on startup: {}, is allow start when expired: {}",
                properties.getStoragePath(), properties.isValidateOnStartup(), properties.isAllowStartWhenExpired());

        LicenseManager licenseManager = new LicenseManager(toClientProperties(properties), validator, eventListener);
        licenseManager.init();
        return licenseManager;
    }

    @Bean
    @ConditionalOnClass(name = "org.aspectj.lang.ProceedingJoinPoint")
    @ConditionalOnMissingBean
    public LicenseAspect licenseAspect(LicenseManager licenseManager) {
        log.info("license aspect init ...");
        return new LicenseAspect(licenseManager);
    }

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

    @Bean
    @ConditionalOnMissingBean(name = "defaultLicenseEventListener")
    @ConditionalOnProperty(
            prefix = "license",
            name = "enable-default-listener",
            havingValue = "true",
            matchIfMissing = false
    )
    public DefaultLicenseEventListener defaultLicenseEventListener() {
        log.info("license default event listener init ...");
        return new DefaultLicenseEventListener();
    }

    private LicenseClientProperties toClientProperties(LicenseProperties properties) {
        LicenseClientProperties clientProperties = new LicenseClientProperties();
        clientProperties.setEnabled(properties.isEnabled());
        clientProperties.setStoragePath(properties.getStoragePath());
        clientProperties.setValidateOnStartup(properties.isValidateOnStartup());
        clientProperties.setAllowStartWhenExpired(properties.isAllowStartWhenExpired());
        clientProperties.setExpiryWarningDays(properties.getExpiryWarningDays());
        clientProperties.setApiPrefix(properties.getApiPrefix());
        return clientProperties;
    }
}
