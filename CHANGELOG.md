# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [0.1.0] - 2026-07-16

### Added

- **openbar-auth**: Microserviço de identidade e acesso
  - Autenticação via JWT (RS256)
  - CRUD de usuários com roles (ADMIN, MANAGER, WAITER, CASHIER, KITCHEN)
  - BCrypt (strength=12) para hashing de senhas
  - Flyway para migrações de banco de dados
  - Swagger/OpenAPI via springdoc-openapi
  - Health checks via Spring Actuator
- **CI/CD Pipeline** (GitHub Actions)
  - Build & Test com Gradle
  - SonarQube integration (coverage, quality gate)
  - Docker multi-stage build
  - Postman/Newman API tests
- **Code Quality**
  - JaCoCo coverage (94.4%)
  - Detekt static analysis (0 issues)
  - 42 testes unitários e de integração
- **Infrastructure**
  - Docker Compose (PostgreSQL, Redis, Kafka, Zookeeper)
  - Branch protection rules (develop → main via PR)
  - Release workflow automatizado

### Changed

- Nenhuma alteração (primeira release)

### Deprecated

- Nenhuma depreciação

### Removed

- Nenhuma remoção

### Fixed

- Nenhuma correção (primeira release)

### Security

- JWT validation stateless per-service (sem chamadas inter-service)
- Secrets nunca commitados (.env-exemple é template)

[0.1.0]: https://github.com/douglas-dreer/openbar-auth/releases/tag/v0.1.0
