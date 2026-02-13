# 发布指南

本文档定义 `ilicense-client-java` 的发布流程。

## 版本策略

采用语义化版本：

- `MAJOR`：不兼容 API 变更
- `MINOR`：向后兼容的新功能
- `PATCH`：向后兼容的问题修复

示例：

- `1.0.0`
- `1.1.0`
- `1.1.1`

## 分支与标签规范

- 发布分支（可选）：`release/x.y.z`
- Git 标签：`vX.Y.Z`（例如：`v1.1.0`）

## 发布前检查

发布前请确认：

- CI 全绿（`compile` 与 `unit_test`）
- `CHANGELOG.md` 的 `[Unreleased]` 已完整整理
- `README.md` 与模块文档已同步更新
- 不包含仅用于调试的临时改动

本地执行：

```bash
mvn clean compile
mvn test
```

## 凭据准备

### 本地发布（`~/.m2/settings.xml`）

请配置 `server` 凭据，`id` 需与 `pom.xml` 中一致：

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>${env.OSSRH_USERNAME}</username>
      <password>${env.OSSRH_TOKEN}</password>
    </server>
    <server>
      <id>github</id>
      <username>${env.GITHUB_ACTOR}</username>
      <password>${env.GITHUB_TOKEN}</password>
    </server>
  </servers>
</settings>
```

如发布到 Central，还需要本地 GPG key 并可用于 `maven-gpg-plugin`。

### CI 凭据

- GitHub Actions（`.github/workflows/publish.yml`）
  - `OSSRH_USERNAME`
  - `OSSRH_TOKEN`
  - `GPG_PRIVATE_KEY`
  - `GPG_PASSPHRASE`

## 发布步骤

### 1) 确定发布版本

假设目标版本为 `X.Y.Z`。

### 2) 更新 Maven 版本号

在仓库根目录执行：

```bash
mvn -q versions:set -DnewVersion=X.Y.Z -DgenerateBackupPoms=false
```

### 3) 整理变更记录

编辑 `CHANGELOG.md`：

- 将 `[Unreleased]` 内容迁移到新段落：`## [X.Y.Z] - YYYY-MM-DD`
- 保留新的空 `[Unreleased]` 段落用于后续迭代

### 4) 提交发布改动

```bash
git add pom.xml ilicense-client/pom.xml ilicense-spring-boot/pom.xml \
  ilicense-spring-boot/ilicense-spring-boot-autoconfigure/pom.xml \
  ilicense-spring-boot/ilicense-spring-boot-starter/pom.xml CHANGELOG.md

git commit -m "chore(release): vX.Y.Z"
```

### 5) 打标签并推送

```bash
git tag -a vX.Y.Z -m "Release vX.Y.Z"
git push origin main
git push origin vX.Y.Z
```

### 6) 发布制品

按目标仓库执行（可多次执行）：

```bash
# 发布到 Maven Central
mvn -P publish-central clean deploy

# 发布到 GitHub Packages
mvn -P publish-github clean deploy
```

建议发布后验证：

- `ilicense-client` 可在非 Spring 项目正常引入使用
- `ilicense-spring-boot-starter` 可在 Spring Boot 项目正常引入使用

## 发布后处理

### 1) 回到下一开发快照版本

```bash
mvn -q versions:set -DnewVersion=X.Y.(Z+1)-SNAPSHOT -DgenerateBackupPoms=false
```

### 2) 提交快照版本改动

```bash
git add pom.xml ilicense-client/pom.xml ilicense-spring-boot/pom.xml \
  ilicense-spring-boot/ilicense-spring-boot-autoconfigure/pom.xml \
  ilicense-spring-boot/ilicense-spring-boot-starter/pom.xml

git commit -m "chore: start next development iteration"
git push origin main
```

## 回滚说明

- 若标签错误且未被使用，可删除后重建。
- 若制品已发布，不要覆盖同版本，请发布新的补丁版本。

## CI 模板说明

- GitHub 发布模板：`.github/workflows/publish.yml`
  - 手动触发，选择 `central` / `github` 目标
