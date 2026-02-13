# 变更记录

本文件记录项目的重要变更。

本项目遵循语义化版本原则（SemVer）。

## [Unreleased]

### 新增

- 将 `ilicense-client` 抽离为零 Spring 依赖的核心 SDK 模块
- 增加核心事件监听抽象（激活/过期/即将过期）
- 在 Spring Boot 自动配置模块增加事件桥接并发布 Spring 事件
- 增加 `ilicense-client` README，并优化仓库/模块文档
- 增加 `ilicense-client` 核心单元测试（validator/manager）
- 增加 `ilicense-spring-boot-autoconfigure` 集成测试
- 增加 GitLab CI 流水线（`compile` 与 `unit_test`）

### 变更

- `ilicense-spring-boot-autoconfigure` 改为依赖 `ilicense-client` 提供核心许可证逻辑
- 根构建增加 JUnit 5 与 Surefire 配置

### 移除

- 删除 Spring 自动配置模块中重复的 core/exception 实现
- 删除 `ilicense-client` 中 Spring Boot 应用骨架
