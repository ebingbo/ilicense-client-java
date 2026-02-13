# Contributing Guide

Thanks for contributing to `ilicense-client-java`.

## Scope

This repository contains:

- `ilicense-client`: core Java SDK (no Spring dependency)
- `ilicense-spring-boot`: Spring Boot starter/autoconfiguration

Please keep framework-independent logic in `ilicense-client`, and Spring integration logic in `ilicense-spring-boot`.

## Development Setup

Requirements:

- JDK 21
- Maven 3.9+

Build:

```bash
mvn clean compile
```

Run tests:

```bash
mvn test
```

## Branch and Commit

- Branch from `main`
- Keep changes focused and small
- Use clear commit messages

Recommended commit format:

```text
type(scope): short summary
```

Examples:

- `feat(client): add license event listener interface`
- `fix(autoconfigure): map spring properties to core properties`
- `test(client): cover activation persistence`

## Pull Request Checklist

Before opening a PR, ensure:

- Code compiles: `mvn clean compile`
- Tests pass: `mvn test`
- README/docs updated if behavior/config changed
- No unrelated formatting or refactor noise

PR should include:

- Problem and motivation
- Solution summary
- Compatibility impact
- Test evidence

## API and Compatibility

- Avoid breaking public API without strong reason
- If a breaking change is required, document migration steps in PR and `CHANGELOG.md`
- Keep default behavior backward compatible whenever possible

## Reporting Issues

Please use GitLab Issues and include:

- Version used
- Reproduction steps
- Expected behavior
- Actual behavior
- Relevant logs/errors
