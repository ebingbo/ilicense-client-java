# ilicense-client

`ilicense-client` 是 iLicense 的 Java 核心 SDK，零 Spring 依赖，适用于任意 Java 运行环境。

## 适用场景

- 普通 Java 应用（`main`）
- Servlet/Jakarta EE 应用
- 非 Spring 框架（如 Micronaut、Quarkus 等）
- 需要把 License 能力嵌入已有平台但不希望引入 Spring 依赖

## 核心能力

- 激活码验签与解析（RSA + SHA256）
- License 本地存储与加载
- 启动校验、手动校验、模块权限校验
- 到期/即将到期/激活事件回调

## Maven 依赖

```xml
<dependency>
    <groupId>com.ibingbo</groupId>
    <artifactId>ilicense-client</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## 最小示例

```java
import com.ibingbo.ilicense.config.LicenseClientProperties;
import com.ibingbo.ilicense.core.LicenseManager;
import com.ibingbo.ilicense.core.LicenseValidator;
import com.ibingbo.ilicense.event.LicenseEventListener;

public class Demo {
    public static void main(String[] args) {
        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A...";

        LicenseClientProperties properties = new LicenseClientProperties();
        properties.setEnabled(true);
        properties.setStoragePath("/data/app/license.dat");
        properties.setValidateOnStartup(true);
        properties.setAllowStartWhenExpired(false);
        properties.setExpiryWarningDays(30);

        LicenseValidator validator = new LicenseValidator(publicKey);
        LicenseEventListener listener = new LicenseEventListener() {
            @Override
            public void onActivated(com.ibingbo.ilicense.core.LicenseInfo info) {
                System.out.println("license activated: " + info.getCustomerName());
            }

            @Override
            public void onExpired(com.ibingbo.ilicense.core.LicenseInfo info) {
                System.err.println("license expired: " + info.getExpireAt());
            }
        };

        LicenseManager manager = new LicenseManager(properties, validator, listener);
        manager.init();

        // 激活（示例）
        // manager.activate("<activation-code>");

        // 业务检查
        manager.checkLicense();
        manager.checkModule("advanced-module");
    }
}
```

## 关键 API

- `LicenseValidator#validate(String activationCode)`
- `LicenseManager#init()`
- `LicenseManager#activate(String activationCode)`
- `LicenseManager#checkLicense()`
- `LicenseManager#checkModule(String moduleName)`
- `LicenseManager#checkLicenseStatus()`
- `LicenseManager#getCurrentLicense()`

## 异常说明

- `LicenseException`：基础异常
- `LicenseNotFoundException`：未激活
- `LicenseExpiredException`：已过期
