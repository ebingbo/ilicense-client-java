# ilicense-spring-boot

`ilicense-spring-boot` 提供 Spring Boot 集成能力，底层依赖 `ilicense-client` 核心 SDK。

## 模块说明

- `ilicense-spring-boot-starter`
  - 给业务应用直接引入
- `ilicense-spring-boot-autoconfigure`
  - 自动装配实现（通常由 starter 间接引入）

## 主要能力

- 自动创建 `LicenseValidator`、`LicenseManager`
- 启动时 License 校验
- 定时校验（可配置间隔）
- License REST API（可开关）
- AOP 注解校验（`@RequireLicense`）
- Web 拦截器校验（可开关）
- Spring 事件发布（激活/过期/即将过期）

## 依赖

```xml
<dependency>
    <groupId>com.ibingbo</groupId>
    <artifactId>ilicense-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## 配置示例

```yaml
license:
  enabled: true
  public-key: MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A...
  storage-path: /data/app/license.dat

  validate-on-startup: true
  allow-start-when-expired: false

  enable-api: true
  api-prefix: /api/license

  check-interval: 1h
  expiry-warning-days: 30
  enable-scheduled-check: true

  enable-default-listener: false

  enable-interceptor: false
  intercept-url-patterns:
    - /**
  exclude-url-patterns:
    - /api/license/**
    - /error
    - /favicon.ico
```

## 使用方式

### 1) 注解方式

```java
import com.ibingbo.ilicense.annotation.RequireLicense;

@RequireLicense(module = "advanced-module")
public void advancedFeature() {
    // business logic
}
```

### 2) 编程式方式

```java
import com.ibingbo.ilicense.core.LicenseManager;

public class BizService {
    private final LicenseManager licenseManager;

    public BizService(LicenseManager licenseManager) {
        this.licenseManager = licenseManager;
    }

    public void run() {
        licenseManager.checkLicense();
        licenseManager.checkModule("advanced-module");
    }
}
```

## 事件监听示例

```java
import com.ibingbo.ilicense.event.LicenseActivatedEvent;
import com.ibingbo.ilicense.event.LicenseExpiredEvent;
import com.ibingbo.ilicense.event.LicenseExpiringSoonEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MyLicenseListener {

    @EventListener
    public void onActivated(LicenseActivatedEvent event) {
        // activated
    }

    @EventListener
    public void onExpired(LicenseExpiredEvent event) {
        // expired
    }

    @EventListener
    public void onExpiringSoon(LicenseExpiringSoonEvent event) {
        // expiring soon
    }
}
```
