package io.github.ebingbo.ilicense.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;

@Data
public class LicenseInfo implements Serializable {

    @JsonProperty("license_code")
    private String licenseCode;

    @JsonProperty("customer_code")
    private String customerCode;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("issuer_code")
    private String issuerCode;

    @JsonProperty("issuer_name")
    private String issuerName;

    @JsonProperty("issue_at")
    private OffsetDateTime issueAt;

    @JsonProperty("expire_at")
    private OffsetDateTime expireAt;

    @JsonProperty("modules")
    private String modules;

    @JsonProperty("max_instances")
    private Integer maxInstances;

    @JsonProperty("valid")
    @JsonIgnore
    private transient boolean valid;

    @JsonProperty("days_left")
    @JsonIgnore
    private transient long daysLeft;

    public boolean isExpired() {
        return expireAt != null && expireAt.toInstant().isBefore(Instant.now());
    }

    public boolean hasModule(String moduleName) {
        return modules != null && modules.contains(moduleName);
    }
}
