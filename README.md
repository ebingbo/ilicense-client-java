# ilicense-client-java

`ilicense-client-java` 是 iLicense 在 Java 侧的客户端实现，包含：

- `ilicense-client`：零 Spring 依赖的核心 SDK
- `ilicense-spring-boot`：基于核心 SDK 的 Spring Boot Starter / AutoConfiguration

## 模块结构

```text
ilicense-client-java
├── ilicense-client                       # 核心 SDK（纯 Java）
└── ilicense-spring-boot
    ├── ilicense-spring-boot-autoconfigure
    └── ilicense-spring-boot-starter
```

## 依赖关系

```text
业务应用(非 Spring) -> ilicense-client
业务应用(Spring Boot) -> ilicense-spring-boot-starter -> ilicense-spring-boot-autoconfigure -> ilicense-client
```

## 快速选择

- 非 Spring Boot 项目：使用 `ilicense-client`
- Spring Boot 项目：使用 `ilicense-spring-boot-starter`

## 构建

在仓库根目录执行：

```bash
mvn clean compile
```

## 文档入口

- 核心 SDK 文档：`ilicense-client/README.md`
- Spring Boot 集成文档：`ilicense-spring-boot/README.md`
- 贡献指南：`CONTRIBUTING.md`
- 行为准则：`CODE_OF_CONDUCT.md`
- 安全策略：`SECURITY.md`
- 变更记录：`CHANGELOG.md`
- 发布流程：`RELEASE.md`
- Issue 模板：`.gitlab/issue_templates/`
- MR 模板：`.gitlab/merge_request_templates/`

## License

本仓库采用 `LICENSE` 文件中定义的许可证。
