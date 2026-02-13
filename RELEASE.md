# Release Guide

This document defines the release workflow for `ilicense-client-java`.

## Versioning Strategy

Use semantic versioning:

- `MAJOR`: incompatible API changes
- `MINOR`: backward-compatible features
- `PATCH`: backward-compatible fixes

Examples:

- `1.0.0`
- `1.1.0`
- `1.1.1`

## Branch and Tag Convention

- Release branch (optional): `release/x.y.z`
- Git tag: `vX.Y.Z` (example: `v1.1.0`)

## Pre-release Checklist

Before releasing, verify:

- CI is green (`compile` and `unit_test`)
- `CHANGELOG.md` has complete `[Unreleased]` notes
- `README.md` and module docs are up to date
- No snapshot-only or local debugging artifacts are included

Run locally:

```bash
mvn clean compile
mvn test
```

## Release Steps

### 1) Decide release version

Assume target version is `X.Y.Z`.

### 2) Update Maven versions

From repository root:

```bash
mvn -q versions:set -DnewVersion=X.Y.Z -DgenerateBackupPoms=false
```

### 3) Finalize changelog

In `CHANGELOG.md`:

- Move items from `[Unreleased]` to a new section: `## [X.Y.Z] - YYYY-MM-DD`
- Keep a fresh empty `[Unreleased]` section for next cycle

### 4) Commit release changes

```bash
git add pom.xml ilicense-client/pom.xml ilicense-spring-boot/pom.xml \
  ilicense-spring-boot/ilicense-spring-boot-autoconfigure/pom.xml \
  ilicense-spring-boot/ilicense-spring-boot-starter/pom.xml CHANGELOG.md

git commit -m "chore(release): vX.Y.Z"
```

### 5) Create and push tag

```bash
git tag -a vX.Y.Z -m "Release vX.Y.Z"
git push origin main
git push origin vX.Y.Z
```

### 6) Publish artifacts

Publish by your configured distribution channel (GitLab Package Registry / Maven repository).

Suggested verification:

- `ilicense-client` can be resolved and used in non-Spring project
- `ilicense-spring-boot-starter` can be resolved in Spring Boot app

## Post-release

### 1) Bump back to next snapshot

```bash
mvn -q versions:set -DnewVersion=X.Y.(Z+1)-SNAPSHOT -DgenerateBackupPoms=false
```

### 2) Commit snapshot bump

```bash
git add pom.xml ilicense-client/pom.xml ilicense-spring-boot/pom.xml \
  ilicense-spring-boot/ilicense-spring-boot-autoconfigure/pom.xml \
  ilicense-spring-boot/ilicense-spring-boot-starter/pom.xml

git commit -m "chore: start next development iteration"
git push origin main
```

## Rollback Notes

- If tag was created incorrectly and not consumed, delete and recreate it.
- If artifacts were published, never overwrite the same version. Publish a new patch version instead.
