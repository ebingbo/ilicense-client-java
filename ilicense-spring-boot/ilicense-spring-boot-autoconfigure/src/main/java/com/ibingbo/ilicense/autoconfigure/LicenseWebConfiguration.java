package com.ibingbo.ilicense.autoconfigure;

import com.ibingbo.ilicense.config.LicenseProperties;
import com.ibingbo.ilicense.core.LicenseManager;
import com.ibingbo.ilicense.interceptor.LicenseInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(
        prefix = "license",
        name = "enable-interceptor",
        havingValue = "true",
        matchIfMissing = true
)
public class LicenseWebConfiguration implements WebMvcConfigurer {
    private final LicenseProperties properties;
    private final LicenseManager licenseManager;

    public LicenseWebConfiguration(LicenseProperties properties,
                                   LicenseManager licenseManager) {
        this.properties = properties;
        this.licenseManager = licenseManager;
    }

    @Bean
    public LicenseInterceptor licenseInterceptor() {
        log.info("license interceptor init ...");
        return new LicenseInterceptor(licenseManager, properties);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("license interceptor register ... add path pattern: {}, exclude path pattern: {}", properties.getInterceptUrlPatterns(), properties.getExcludeUrlPatterns());
        registry.addInterceptor(licenseInterceptor())
                .addPathPatterns(properties.getInterceptUrlPatterns())
                .excludePathPatterns(properties.getExcludeUrlPatterns());
    }
}
