# Changelog

All notable changes to this project will be documented in this file.

This project follows semantic versioning principles.

## [Unreleased]

### Added

- Extracted `ilicense-client` as a zero-Spring core SDK module
- Added core event listener abstraction for activation/expiry notifications
- Added integration bridge in Spring Boot autoconfigure to publish Spring events
- Added `ilicense-client` README and improved project/module documentation
- Added unit tests for `ilicense-client` validator/manager
- Added integration tests for `ilicense-spring-boot-autoconfigure`
- Added GitLab CI pipeline (`compile` and `unit_test` stages)

### Changed

- `ilicense-spring-boot-autoconfigure` now depends on `ilicense-client` for core license logic
- Root build now includes JUnit 5 and Surefire configuration

### Removed

- Removed duplicated core/exception implementations from Spring autoconfigure module
- Removed Spring Boot application skeleton from `ilicense-client`
