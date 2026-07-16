# openbar-auth Knowledge Base

## Decision Log

### 1. JWT com JJWT 0.12.6 (HMAC-SHA, não RS256)
**Decisão**: Usar HMAC-SHA com secret simétrico em vez de RS256 com chaves assimétricas.
**Razão**: Simplicidade para o primeiro serviço. RS256 pode ser adicionado depois via config de chaves públicas/privadas.
**Impacto**: O token é assinado com um secret compartilhado. Para produção, migrar para RS256.

### 2. BCrypt Workload 12
**Decisão**: Usar BCrypt com cost factor 12.
**Razão**: Equilíbrio entre segurança e performance. 12 é o padrão recomendado para 2024+.

### 3. Soft Delete para Usuários
**Decisão**: Usar campo `active` ao invés de DELETE físico.
**Razão**: Preserva histórico de pedidos/turnos vinculados ao usuário.
**Impacto**: Queries devem filtrar por `active = true` por padrão.

### 4. Flyway para Migrações
**Decisão**: Scripts SQL em `db/migration` ao invés de Hibernate auto-DDL.
**Razão**: Controle de versão do schema, reprodutibilidade, audit trail.

### 5. RFC 7807 Problem Details
**Decisão**: Implementar globalmente via `@RestControllerAdvice`.
**Razão**: Padrão REST para erros, facilita consumo por clientes.

## Gotchas

1. **Porta padrão**: 8081 (não 8080) — evita conflito com outros serviços.
2. **Hibernate ddl-auto**: `validate` em produção, `create-drop` em testes.
3. **Flyway**: Desabilitado nos testes (usa H2 com schema automático).
4. **JWT Secret**: NUNCA committar o valor real. Usar variável de ambiente.
5. **Username**: Formato email (validado por `@Email`). Pode ser CPF no futuro.

## Patterns

### Estrutura de Controller
```kotlin
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        return ResponseEntity.ok(authService.login(request))
    }
}
```

### Estrutura de Service
```kotlin
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {
    fun login(request: LoginRequest): LoginResponse {
        // Validação → Exceção → GlobalExceptionHandler converte para RFC 7807
    }
}
```

### Exception Handling
```kotlin
@ExceptionHandler(IllegalArgumentException::class)
fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ProblemDetail> {
    return ResponseEntity.badRequest().body(ProblemDetail(...))
}
```

## Testing Strategy

| Tipo | Ferramenta | Cobertura |
|------|-----------|-----------|
| Unit | Mockito + JUnit 5 | Services, JWT |
| Web | WebMvcTest | Controllers, DTOs, Validação |
| Repository | DataJpaTest + H2 | Queries, Constraints |
| Integration | (futuro) Testcontainers | Fluxo completo com PostgreSQL |

## Dependências entre Serviços

- **openbar-auth** é independente — não consome eventos Kafka
- Outros serviços validam JWT localmente (não chamam auth)
- Auth não expõe eventos — é apenas fonte de verdade de identidade

## Commands Úteis

```bash
# Build completo
./gradlew :openbar-auth:build

# Apenas compilar
./gradlew :openbar-auth:compileKotlin

# Testes
./gradlew :openbar-auth:test

# Rodar localmente
./gradlew :openbar-auth:bootRun

# Limpar build
./gradlew :openbar-auth:clean
```
