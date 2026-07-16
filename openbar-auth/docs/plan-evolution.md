# Planejamento — Fases ALTA e MÉDIA

 **Versão:** 0.2.0 (após todas as fases)
 **Data:** 2026-07-16

---

## Visão Geral das Fases

```
Fase 2 (Testcontainers)  ─────┐
Fase 3.1 (Refresh Token) ─────┤──→ v0.2.0
Fase 3.2 (Rate Limiting) ─────┤
Fase 3.3 (JWT Blacklist) ─────┤
Fase 4.1 (Audit Log)     ─────┤
Fase 5 (Role Security)   ─────┘
```

**Regra:** Cada fase = 1 branch `feature/*` → develop → main (squash merge).

---

## Fase 2 — Testcontainers (H2 → PostgreSQL real)

**Prioridade:** ALTA
**Branch:** `feature/020_testcontainers`
**Commits:** `feat/020_*`

### Análise

| Atual | Futuro |
|---|---|
| H2 para testes | PostgreSQL 16 via Testcontainers |
| SQL genérico | SQL específico do PG testado |
| Testes rápidos mas imprecisos | Testes lentos (~2s) mas reais |

### Arquivos a modificar

| Arquivo | Mudança |
|---|---|
| `build.gradle.kts` (root) | Remover H2, adicionar Testcontainers |
| `build.gradle.kts` (openbar-auth) | Adicionar dependências TC |
| `application-test.yml` (novo) | Config TC para testes |
| `UserRepositoryTest.kt` | Adicionar `@ActiveProfiles("test")` |

### Dependências

```kotlin
// build.gradle.kts (root) - subprojects
testImplementation("org.testcontainers:testcontainers:1.19.8")
testImplementation("org.testcontainers:junit-jupiter:1.19.8")
testImplementation("org.testcontainers:postgresql:1.19.8")
testImplementation("org.springframework.boot:spring-boot-testcontainers")
testImplementation("org.springframework.boot:spring-boot-docker-compose")
```

### Arquivos novos

```
openbar-auth/src/test/resources/
  application-test.yml          # Config Testcontainers
openbar-auth/src/test/kotlin/com/openbar/auth/
  TestcontainersConfig.kt       # @TestConfiguration com容器
```

### Critérios de aceite

- [ ] Todos os 42 testes passam com PostgreSQL 16 via TC
- [ ] CI Pipeline passa (Docker já disponível no runner)
- [ ] Coverage > 90%
- [ ] H2 removido do build

### Estimativa: 2-3h de dev

---

## Fase 3.1 — Refresh Token

**Prioridade:** ALTA
**Branch:** `feature/021_refresh_token`
**Commits:** `feat/021_*`

### Análise

| Atual | Futuro |
|---|---|
| Token único (1h) | Access token (1h) + Refresh token (7d) |
| Login frequente | Sessão longa com renovação |

### Fluxo

```
Login → { accessToken (1h), refreshToken (7d) }
  ↓
Access token expira → 401
  ↓
POST /auth/refresh { refreshToken } → { novo accessToken, novo refreshToken }
  ↓
Refresh token expira → 401 → re-login
```

### Arquivos novos

| Arquivo | Camada |
|---|---|
| `RefreshToken.kt` | domain/model |
| `RefreshTokenRepository.kt` | domain/repository |
| `RefreshTokenService.kt` | service |
| `RefreshTokenRequest.kt` | web/dto |
| `RefreshTokenResponse.kt` | web/dto |
| `V2__create_refresh_tokens.sql` | db/migration |

### Arquivos a modificar

| Arquivo | Mudança |
|---|---|
| `LoginResponse.kt` | Adicionar `refreshToken` |
| `AuthService.kt` | Gerar refresh token no login |
| `AuthController.kt` | Novo endpoint POST /refresh |
| `JwtTokenProvider.kt` | Adicionar `jti` (JWT ID) |

### Modelo de dados

```sql
-- V2__create_refresh_tokens.sql
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    revoked BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
```

### Critérios de aceite

- [ ] Login retorna `accessToken` + `refreshToken`
- [ ] POST `/auth/refresh` retorna novos tokens
- [ ] Refresh token expirado retorna 401
- [ ] Refresh token revogado retorna 401
- [ ] Unit tests + Integration tests
- [ ] Coverage > 90%

### Estimativa: 3-4h de dev

---

## Fase 3.2 — Rate Limiting

**Prioridade:** ALTA
**Branch:** `feature/022_rate_limiting`
**Commits:** `feat/022_*`

### Análise

| Atual | Futuro |
|---|---|
| Sem limite de tentativas | 5 tentativas/min por IP |
| Vulnerável a brute force | Resposta 429 Too Many Requests |

### Dependências

```kotlin
// build.gradle.kts
implementation("com.bucket4j:bucket4j-core:8.10.1")
implementation("com.bucket4j:bucket4j-spring-boot-starter:8.10.1")
implementation("org.springframework.boot:spring-boot-starter-data-redis")
```

### Arquivos novos

| Arquivo | Camada |
|---|---|
| `RateLimitConfig.kt` | config |
| `RateLimitFilter.kt` | web/filter |
| `TooManyRequestsException.kt` | exception |

### Arquivos a modificar

| Arquivo | Mudança |
|---|---|
| `SecurityConfig.kt` | Registrar filtro de rate limit |
| `GlobalExceptionHandler.kt` | Handler para 429 |
| `application.yml` | Config Redis + rate limit |

### Config

```yaml
# application.yml
app:
  rate-limit:
    login:
      capacity: 5
      refill-tokens: 5
      refill-duration: 60  # seconds
```

### Critérios de aceite

- [ ] 6ª tentativa de login em 1min retorna 429
- [ ] Contador reseta após 1 minuto
- [ ] Headers `X-Rate-Limit-*` na resposta
- [ ] Testes unitários do filter
- [ ] Coverage > 90%

### Estimativa: 2-3h de dev

---

## Fase 3.3 — JWT Blacklist

**Prioridade:** MÉDIA
**Branch:** `feature/023_jwt_blacklist`
**Commits:** `feat/023_*`

### Análise

| Atual | Futuro |
|---|---|
| Token válido até expirar | Token pode ser revogado (logout) |
| Sem logout efetivo | Logout invalida o token |

### Fluxo

```
Logout → adicionar jti no Redis (TTL = tempo restante do token)
  ↓
Validação de token → verificar se jti está no Redis
  ↓
Se na blacklist → 401 Unauthorized
```

### Dependências

```kotlin
// build.gradle.kts (já tem Redis para refresh token)
// Nenhuma dependência nova necessária
```

### Arquivos novos

| Arquivo | Camada |
|---|---|
| `JwtBlacklistService.kt` | service |
| `JwtBlacklistFilter.kt` | security/filter |

### Arquivos a modificar

| Arquivo | Mudança |
|---|---|
| `JwtTokenProvider.kt` | Adicionar `jti` ao token, verificar blacklist |
| `AuthService.kt` | Novo método `logout(token)` |
| `AuthController.kt` | Novo endpoint POST /logout |
| `SecurityConfig.kt` | Registrar filtro de blacklist |

### Critérios de aceite

- [ ] POST `/auth/logout` invalida o token
- [ ] Token revogado retorna 401
- [ ] Blacklist expira automaticamente (TTL)
- [ ] Testes unitários + integration
- [ ] Coverage > 90%

### Estimativa: 2-3h de dev

---

## Fase 4.1 — Audit Log

**Prioridade:** MÉDIA
**Branch:** `feature/024_audit_log`
**Commits:** `feat/024_*`

### Análise

| Atual | Futuro |
|---|---|
| Sem rastreabilidade | Log de todas as ações sensíveis |
| Não sabe quem fez o quê | userId + action + timestamp + IP |

### Modelo de dados

```sql
-- V3__create_audit_logs.sql
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(50) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
```

### Arquivos novos

| Arquivo | Camada |
|---|---|
| `AuditLog.kt` | domain/model |
| `AuditLogRepository.kt` | domain/repository |
| `AuditLogService.kt` | service |
| `AuditLogAspect.kt` | config (AOP) |
| `AuditLog.kt` | anotacao (annotation) |
| `AuditLogResponse.kt` | web/dto |
| `AuditLogController.kt` | web/controller |
| `V3__create_audit_logs.sql` | db/migration |

### Arquivos a modificar

| Arquivo | Mudança |
|---|---|
| `AuthController.kt` | Adicionar `@Auditable` nos métodos |
| `UserController.kt` | Adicionar `@Auditable` nos métodos |

### Critérios de aceite

- [ ] Login/logout cria registro de auditoria
- [ ] CRUD de usuários cria registros
- [ ] GET `/auth/audit-logs` retorna logs (apenas ADMIN)
- [ ] Testes unitários + integration
- [ ] Coverage > 90%

### Estimativa: 3-4h de dev

---

## Fase 5 — Role-Based Security

**Prioridade:** MÉDIA
**Branch:** `feature/025_role_security`
**Commits:** `feat/025_*`

### Análise

| Atual | Futuro |
|---|---|
| Qualquer user autenticado acessa tudo | Controle por role em cada endpoint |
| Sem `@PreAuthorize` | Security annotations |

### Mapeamento Endpoint → Role

| Endpoint | Roles Permitidas |
|---|---|
| POST /auth/login | ALL |
| POST /auth/refresh | ALL |
| POST /auth/logout | ALL |
| GET /users | ADMIN, MANAGER |
| GET /users/{id} | ALL (próprio perfil) ou ADMIN |
| POST /users | ADMIN |
| PUT /users/{id} | ALL (próprio perfil) ou ADMIN |
| DELETE /users/{id} | ADMIN |
| GET /auth/audit-logs | ADMIN |

### Arquivos a modificar

| Arquivo | Mudança |
|---|---|
| `SecurityConfig.kt` | `@EnableMethodSecurity` |
| `AuthController.kt` | `@PreAuthorize` |
| `UserController.kt` | `@PreAuthorize` |
| `AuditLogController.kt` | `@PreAuthorize` |
| `JwtTokenProvider.kt` | Garantir que role está no token |

### Critérios de aceite

- [ ] Waiter não acessa GET /users
- [ ] Admin acessa tudo
- [ ] User acessa only seu próprio perfil
- [ ] 403 Forbidden para acesso não autorizado
- [ ] Testes de cada cenário
- [ ] Coverage > 90%

### Estimativa: 2-3h de dev

---

## Resumo de Branches

| Fase | Branch | Tipo |
|---|---|---|
| 2 | `feature/020_testcontainers` | feat/* |
| 3.1 | `feature/021_refresh_token` | feat/* |
| 3.2 | `feature/022_rate_limiting` | feat/* |
| 3.3 | `feature/023_jwt_blacklist` | feat/* |
| 4.1 | `feature/024_audit_log` | feat/* |
| 5 | `feature/025_role_security` | feat/* |

### Fluxo por fase

```
feature/02X_* → develop (PR) → main (PR) → tag v0.2.X (opcional)
```

### Ordem de execução

```
1. Testcontainers      (base para testes reais)
2. Refresh Token       (depende de testes reais)
3. Rate Limiting       (independente)
4. JWT Blacklist       (depende de Redis)
5. Audit Log           (depende de role security)
6. Role Security       (base para audit log)
```

**Ordem recomendada:** 2 → 5 → 3.1 → 3.2 → 3.3 → 4.1

---

## Estimativa Total

| Fase | Horas |
|---|---|
| Fase 2 (Testcontainers) | 2-3h |
| Fase 3.1 (Refresh Token) | 3-4h |
| Fase 3.2 (Rate Limiting) | 2-3h |
| Fase 3.3 (JWT Blacklist) | 2-3h |
| Fase 4.1 (Audit Log) | 3-4h |
| Fase 5 (Role Security) | 2-3h |
| **Total** | **14-20h** |

---

## Pré-requisitos por fase

| Fase | Pré-requisito |
|---|---|
| 2 | Docker disponível no CI |
| 3.1 | Fase 2 completa |
| 3.2 | Redis configurado |
| 3.3 | Redis configurado |
| 4.1 | Fase 5 completa |
| 5 | Nenhuma |

---

 **Atualizado:** 2026-07-16
 **Próxima revisão:** Após implementação de cada fase
