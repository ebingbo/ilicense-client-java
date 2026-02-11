package com.ibingbo.sdk.license.core;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class LicenseInfo implements Serializable {

    /**
     * License ID
     */
    private String licenseCode;

    /**
     * 客户ID
     */
    private String customerCode;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 产品代码
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 签发机构代码
     */
    private String issuerCode;
    /**
     * 签发机构名称
     */
    private String issuerName;

    /**
     * 签发日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;

    /**
     * 到期日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    /**
     * 授权模块列表
     */
    private List<String> modules;

    /**
     * 最大实例数
     */
    private Integer maxInstances;

    /**
     * 是否有效
     */
    private transient boolean valid;

    /**
     * 剩余天数
     */
    private transient long daysLeft;

    /**
     * 检查是否过期
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /**
     * 检查是否包含指定模块
     */
    public boolean hasModule(String moduleName) {
        return modules != null && modules.contains(moduleName);
    }
}