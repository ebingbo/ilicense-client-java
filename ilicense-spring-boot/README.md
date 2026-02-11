## 配置示例
### application.yml
```yaml
license:
  #基础配置
  enabled: true
  public-key: MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A...
  storage-path: /data/app/license.dat
  # 启动配置
  validate-on-startup: true
  allow-start-when-expired: false
  # API配置
  enable-api: true
  api-prefix: /api/license
  # 监控配置
  # Duration格式配置 ✅
  check-interval: 1h              # 1小时
  # check-interval: 30m           # 30分钟
  # check-interval: 3600s         # 3600秒
  # check-interval: PT1H          # ISO-8601
  expiry-warning-days: 30
  # 事件监听配置
  enable-default-listener: false  # 禁用默认监听器，使用自定义监听器
  enable-scheduled-check: true

```

## 使用示例
在应用中使用License SDK:

1. 添加依赖:
```xml
<dependency>
   <groupId>com.ibingbo</groupId>
   <artifactId>ilicense-spring-boot-starter</artifactId>
   <version>1.0.0</version>
</dependency>
```
   

2. 配置公钥:
```yaml
license:
   public-key: xxx
```


3. 实现自定义监听器 (可选):
```java
 @Component
public class MyLicenseListener {
   @EventListener
   public void onActivated(LicenseActivatedEvent event) {
   // 自定义处理
   }
}
```
  

4. 使用License检查:
```java
@RequireLicense(module = "高级功能")
public void advancedFeature() {
   // 业务逻辑
}
```
 
