package com.ibingbo.ilicense.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
public class LicenseInfo implements Serializable {

    /**
     * License ID
     */
    @JsonProperty("license_code")
    private String licenseCode;

    /**
     * 客户ID
     */
    @JsonProperty("customer_code")
    private String customerCode;

    /**
     * 客户名称
     */
    @JsonProperty("customer_name")
    private String customerName;

    /**
     * 产品代码
     */
    @JsonProperty("product_code")
    private String productCode;

    /**
     * 产品名称
     */
    @JsonProperty("product_name")
    private String productName;

    /**
     * 签发机构代码
     */
    @JsonProperty("issuer_code")
    private String issuerCode;
    /**
     * 签发机构名称
     */
    @JsonProperty("issuer_name")
    private String issuerName;

    /**
     * 签发日期
     */
    @JsonProperty("issue_at")
    private OffsetDateTime issueAt;

    /**
     * 到期日期
     */
    @JsonProperty("expire_at")
    private OffsetDateTime expireAt;

    /**
     * 授权模块列表
     */
    @JsonProperty("modules")
    private String modules;

    /**
     * 最大实例数
     */
    @JsonProperty("max_instances")
    private Integer maxInstances;

    /**
     * 是否有效
     */
    @JsonProperty("valid")
    @JsonIgnore
    private transient boolean valid;

    /**
     * 剩余天数
     */
    @JsonProperty("days_left")
    @JsonIgnore
    private transient long daysLeft;

    /**
     * 检查是否过期
     */
    public boolean isExpired() {
        return expireAt != null && expireAt.toInstant().isBefore(Instant.now());
    }

    /**
     * 检查是否包含指定模块
     */
    public boolean hasModule(String moduleName) {
        return modules != null && modules.contains(moduleName);
    }
}