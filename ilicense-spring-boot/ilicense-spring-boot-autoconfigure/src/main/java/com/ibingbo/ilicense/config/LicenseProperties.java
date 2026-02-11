package com.ibingbo.ilicense.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * License配置属性
 */
@Data
@ConfigurationProperties(prefix = "license")
public class LicenseProperties {

    /**
     * 是否启用License验证
     */
    private boolean enabled = true;

    /**
     * 公钥（Base64编码）
     */
    private String publicKey;

    /**
     * License存储路径
     */
    private String storagePath = System.getProperty("user.home") + "/.license/license.dat";

    /**
     * 是否在启动时验证License
     */
    private boolean validateOnStartup = false;

    /**
     * License过期后是否允许启动
     */
    private boolean allowStartWhenExpired = true;

    /**
     * 是否启用REST API
     */
    private boolean enableApi = true;

    /**
     * REST API路径前缀
     */
    private String apiPrefix = "/api/license";

    /**
     * 过期提醒天数
     */
    private int expiryWarningDays = 30;

    /**
     * License检查间隔
     * 支持格式: 1h, 30m, 3600s, PT1H
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration checkInterval = Duration.ofHours(1);
    /**
     * 是否启用License拦截器
     */
    private boolean enableInterceptor = false;

    /**
     * 需要拦截的URL模式
     */
    private String[] interceptUrlPatterns = new String[]{"/**"};

    /**
     * 排除拦截的URL模式
     */
    private String[] excludeUrlPatterns = new String[]{
            "/api/license/**",
            "/error",
            "/favicon.ico"
    };

    /**
     * 是否启用默认事件监听器
     */
    private boolean enableDefaultListener = false;
    private boolean enableScheduledCheck = false;


}