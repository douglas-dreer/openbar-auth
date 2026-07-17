# Evolution - openbar-auth

 documento interno de evolução do microsserviço `openbar-auth`.
 Atualizado conforme decisões de开发quipe.

 **Versão atual:** 0.1.0

---

## Status Atual

### Implementado (v0.1.0)

| Componente | Status |
|---|---|
| User entity (UUID, username, passwordHash, role, active) | ✅ |
| UserRole enum (ADMIN, MANAGER, WAITER, CASHIER, KITCHEN) | ✅ |
| UserRepository (Spring Data JPA) | ✅ |
| AuthService (login, JWT generation) | ✅ |
| UserService (CRUD, soft delete) | ✅ |
| AuthController (POST /login) | ✅ |
| UserController (GET, POST, PUT, DELETE) | ✅ |
| JwtTokenProvider (RS256, JJWT 0.12.6) | ✅ |
| BCrypt (strength=12) | ✅ |
| Flyway migration (V1__create_users_table) | ✅ |
| GlobalExceptionHandler (RFC 7807 Problem Details) | ✅ |
| Swagger/OpenAPI (springdoc-openapi) | ✅ |
| Spring Actuator (health, info) | ✅ |
| 42 testes (unit + controller + repository) | ✅ |
| CI/CD Pipeline (GitHub Actions) | ✅ |
| SonarQube integration (94.4% coverage) | ✅ |
| Docker multi-stage build | ✅ |
| Postman collection (11 requests) | ✅ |

### Limitações Conhecidas

| Limitação | Impacto | Solução |
|---|---|---|
| ~~Testes usam H2 (não PostgreSQL)~~ | ~~Não testa SQL específico do PG~~ | ✅ Resolvido (PostgreSQL 16 real) |
| ~~Sem refresh token~~ | ~~Usuário precisa logar novamente a cada 1h~~ | ✅ Resolvido (Refresh token PostgreSQL, TTL 7 dias) |
| ~~Sem rate limiting~~ | ~~Vulnerável a brute force no login~~ | ✅ Resolvido (Bucket4j, 5 req/min/IP) |
| Sem auditoria de login | Não sabe quem logou quando | Adicionar AuditLog |
| JWT não pode ser revogado | Token roubado fica válido até expirar | Blacklist no Redis |
| ~~Sem role-based security~~ | ~~Qualquer user autenticado acessa tudo~~ | ✅ Resolvido (Method security + @PreAuthorize) |
| Sem multi-tenancy | Não suporta múltiplas filiais | TenantInterceptor |

---

## Roadmap de Evolução

### ~~Fase 2 — Integração com PostgreSQL (Prioridade: ALTA)~~ ✅ CONCLUÍDA

**Objetivo:** Testar contra o mesmo banco usado em produção.

**Implementado:** PostgreSQL 16 real via container Docker (porta 5433)
- `@AutoConfigureTestDatabase(replace = NONE)` no `UserRepositoryTest`
- `application-test.yml` com JDBC URL padrão
- Todos os 42 testes passam contra PostgreSQL 16

---

### Fase 3 — Segurança e JWT (Prioridade: ALTA)

#### ~~3.1 Refresh Token~~ ✅ CONCLUÍDA

**Implementado:** PostgreSQL storage, UUID token, TTL 7 dias, rotacao no refresh.

**Implementação:**
- Novo endpoint: `POST /api/v1/auth/refresh`
- Refresh token armazenado no Redis (TTL 7 dias)
- Response inclui `accessToken` + `refreshToken`
- Refresh token é UUID, não JWT

**Arquivos a criar:**
- `RefreshTokenService.kt`
- `RefreshTokenRepository.kt` (Redis)
- `RefreshTokenResponse.kt` (DTO)
- Migration: `V2__create_refresh_tokens_table.sql` (ou Redis)

#### ~~3.2 Rate Limiting no Login~~ ✅ CONCLUÍDA

**Implementado:** Bucket4j (token bucket) in-memory, 5 req/min/IP, RFC 7807 429.

**Implementação:**
- Spring Boot + Redis + Bucket4j
- Limite: 5 tentativas/minuto por IP
- Response 429 Too Many Requests

**Arquivos a criar:**
- `RateLimitConfig.kt`
- `RateLimitFilter.kt`

#### 3.3 JWT Blacklist (Revogação)

**Motivo:** Logout efetivo e revogação de tokens comprometidos.

**Implementação:**
- Ao fazer logout, adicionar JWT ID (jti) no Redis com TTL = tempo restante do token
- JwtTokenProvider verifica blacklist antes de validar

**Arquivos a modificar:**
- `JwtTokenProvider.kt` — adicionar verificação de blacklist
- `AuthService.kt` — adicionar logout

---

### Fase 4 — Auditoria e Logging (Prioridade: MÉDIA)

#### 4.1 Audit Log

**Motivo:** Rastreabilidade de ações sensíveis.

**Implementação:**
- Nova entidade: `AuditLog` (id, userId, action, details, timestamp, ip)
- Interceptor ou AOP para gravar ações
- Endpoints: `GET /api/v1/auth/audit-logs` (apenas ADMIN)

**Migration:** `V3__create_audit_logs_table.sql`

#### 4.2 Estrutura de Logs

**Motivo:** Observabilidade em produção.

**Implementação:**
- Logback com JSON format (ELK/Datadog compatible)
- Request ID em todas as respostas
- Slow query logging (> 500ms)

---

### ~~Fase 5 — Role-Based Security (Prioridade: MÉDIA)~~ ✅ CONCLUÍDA

**Objetivo:** Controle de acesso granular.

**Implementado:**
- SecurityFilterChain stateless + JWT filter
- @EnableMethodSecurity + @PreAuthorize nos controllers
- CustomPermissionEvaluator para acesso "own profile"
- JwtAuthenticationEntryPoint (401 RFC 7807)
- UserIdPrincipal para representar usuario autenticado
- SecurityIntegrationTest (6 testes de autorizacao)

**Mudanças necessárias:**
- Habilitar `@EnableMethodSecurity` no SecurityConfig
- Adicionar `@PreAuthorize` nos controllers
- Custom PermissionEvaluator se necessário

**Endpoint → Role mapping:**

| Endpoint | Roles Permitidas |
|---|---|
| POST /login | ALL |
| GET /users | ADMIN, MANAGER |
| GET /users/{id} | ALL (próprio perfil) ou ADMIN |
| POST /users | ADMIN |
| PUT /users/{id} | ALL (próprio perfil) ou ADMIN |
| DELETE /users/{id} | ADMIN |

---

### Fase 6 — Multi-Tenancy (Prioridade: BAIXA)

**Motivo:** Suporte a múltiplas filiais do OPENBAR.

**Implementação:**
- Header `X-Tenant-ID` em todas as requisições
- TenantInterceptor extrai tenant do JWT ou header
- Flyway migrations por tenant
- Connection pool por tenant (HikariCP)

**Nota:** Considerar se multi-tenancy será por schema ou por row.

---

### Fase 7 — Performance e Observabilidade (Prioridade: BAIXA)

| Item | Descrição |
|---|---|
| Redis Cache | Cache de usernames (evita lookup no DB a cada login) |
| Connection Pool Tuning | HikariCP metrics + tuning |
| Micrometer + Prometheus | Métricas de negócio (logins/min, users criados) |
| Distributed Tracing | OpenTelemetry + Jaeger |
| Health Checks | Custom indicators (DB, Redis, Kafka) |

---

## Prioridades

| Fase | Prioridade | Esforço | Impacto |
|---|---|---|---|
| ~~2 — Testcontainers~~ | ~~ALTA~~ | ~~Médio~~ | ~~Alto (confiança nos testes)~~ ✅ |
| ~~3.1 — Refresh Token~~ | ~~ALTA~~ | ~~Médio~~ | ~~Alto (UX)~~ ✅ |
| ~~3.2 — Rate Limiting~~ | ~~ALTA~~ | ~~Baixo~~ | ~~Alto (segurança)~~ ✅ |
| 3.3 — JWT Blacklist | MÉDIA | Baixo | Médio (segurança) |
| 4.1 — Audit Log | MÉDIA | Médio | Médio (compliance) |
| ~~5 — Role Security~~ | ~~MÉDIA~~ | ~~Médio~~ | ~~Alto (controle de acesso)~~ ✅ |
| 6 — Multi-Tenancy | BAIXA | Alto | Alto (escala) |
| 7 — Observabilidade | BAIXA | Médio | Médio (operações) |

---

## Decisões Técnicas

| Decisão | Justificativa |
|---|---|
| Testcontainers > H2 | H2 não suporta todos os tipos PostgreSQL (JSONB, arrays, etc.) |
| Refresh token via Redis | Performance > DB para tokens temporários |
| Bucket4j para rate limiting | Integração nativa com Spring Boot + Redis |
| Method security > Filter | Mais granular, harder to bypass, padrão Spring |
| Multi-tenancy por row | Mais simples que por schema, suficiente para poucas filiais |

---

## Referências

- [Spring Authorization Server](https://spring.io/projects/spring-authorization-server)
- [Testcontainers](https://www.testcontainers.org/)
- [Bucket4j](https://github.com/bucket4j/bucket4j)
- [Spring Security Method Security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)

---

 **Atualizado:** 2026-07-16
 **Próxima revisão:** Após implementação da Fase 3.3 (JWT Blacklist)
