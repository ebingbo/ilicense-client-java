# 贡献指南

感谢你为 `ilicense-client-java` 做贡献。

## 仓库范围

本仓库包含：

- `ilicense-client`：核心 Java SDK（无 Spring 依赖）
- `ilicense-spring-boot`：Spring Boot Starter / 自动配置

请将框架无关的逻辑放在 `ilicense-client`，将 Spring 集成相关逻辑放在 `ilicense-spring-boot`。

## 开发环境

要求：

- JDK 21
- Maven 3.9+

构建：

```bash
mvn clean compile
```

测试：

```bash
mvn test
```

## 分支与提交

- 从 `main` 分支拉取开发分支
- 单次提交尽量聚焦、改动尽量小
- 使用清晰的提交信息

建议格式：

```text
type(scope): 简要说明
```

示例：

- `feat(client): 增加许可证事件监听接口`
- `fix(autoconfigure): 修复 spring 配置映射`
- `test(client): 补充激活持久化测试`

## 合并请求检查清单

提交 PR 前请确认：

- 编译通过：`mvn clean compile`
- 测试通过：`mvn test`
- 行为或配置变更已更新 README/文档
- 不包含无关格式化或重构噪音

PR 描述建议包含：

- 问题背景与动机
- 方案说明
- 兼容性影响
- 测试与验证结果

## API 与兼容性

- 非必要不要破坏公开 API
- 如需破坏性变更，必须在 PR 和 `CHANGELOG.md` 中提供迁移说明
- 默认行为尽量保持向后兼容

## 问题反馈

请通过 GitLab Issue 提交，并尽量提供：

- 使用版本
- 复现步骤
- 期望行为
- 实际行为
- 相关日志/错误堆栈
