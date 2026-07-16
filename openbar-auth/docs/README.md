# openbar-auth

Microserviço de identidade e acesso do OPENBAR. Responsável por autenticação via JWT e CRUD de usuários.

## Stack

- Kotlin 2.1.10 / JVM 21
- Spring Boot 3.4.7
- PostgreSQL 16+ (via Flyway)
- JJWT 0.12.6 (RS256)
- Spring Security Crypto (BCrypt Workload 12)

## Endpoints

| Método | Path | Descrição |
|--------|------|-----------|
| `POST` | `/api/v1/auth/login` | Autenticar usuário, retorna JWT |
| `GET` | `/api/v1/auth/users` | Listar usuários (paginado) |
| `GET` | `/api/v1/auth/users/{id}` | Buscar usuário por ID |
| `POST` | `/api/v1/auth/users` | Criar novo usuário |
| `PUT` | `/api/v1/auth/users/{id}` | Atualizar usuário |
| `DELETE` | `/api/v1/auth/users/{id}` | Soft delete (active=false) |

### POST /api/v1/auth/login

```json
// Request
{ "username": "admin@example.com", "password": "123456" }

// Response 200
{ "accessToken": "eyJhb...", "expiresIn": 3600 }
```

### POST /api/v1/auth/users

```json
// Request
{
  "username": "garcom@example.com",
  "password": "123456",
  "role": "WAITER"
}

// Response 201
{
  "id": "uuid",
  "username": "garcom@example.com",
  "role": "WAITER",
  "active": true
}
```

## Roles

| Role | Descrição |
|------|-----------|
| `ADMIN` | Acesso total ao sistema |
| `MANAGER` | Gerente de operações |
| `WAITER` | Garçom - operações de salão |
| `CASHIER` | Operador de caixa/PDV |
| `KITCHEN` | Cozinheiro/barman (KDS) |

## Configuração

Variáveis de ambiente (via `.env`):

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=openbar_auth
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=suaChaveSecretaAqui
```

## Build & Run

```bash
# Compilar
./gradlew :openbar-auth:compileKotlin

# Rodar testes
./gradlew :openbar-auth:test

# Build completo (com testes)
./gradlew :openbar-auth:build

# Rodar o serviço
./gradlew :openbar-auth:bootRun
```

O serviço inicia na porta **8081**.

## Testes

- **Unitários**: `AuthServiceTest`, `UserServiceTest` (mocks)
- **Controller**: `AuthControllerTest`, `UserControllerTest` (WebMvcTest)
- **Repository**: `UserRepositoryTest` (DataJpaTest com H2)

## Estrutura

```
openbar-auth/
├── src/main/kotlin/com/openbar/auth/
│   ├── OpenBarAuthApplication.kt        # Entry point
│   ├── config/SecurityConfig.kt          # BCrypt encoder
│   ├── domain/
│   │   ├── model/
│   │   │   ├── User.kt                  # Entidade JPA
│   │   │   └── UserRole.kt              # Enum de roles
│   │   └── repository/
│   │       └── UserRepository.kt         # Spring Data JPA
│   ├── security/
│   │   └── JwtTokenProvider.kt           # Geração/validação JWT
│   ├── service/
│   │   ├── AuthService.kt               # Lógica de login
│   │   └── UserService.kt               # CRUD de usuários
│   └── web/
│       ├── controller/
│       │   ├── AuthController.kt         # POST /login
│       │   └── UserController.kt         # CRUD endpoints
│       ├── dto/
│       │   ├── LoginRequest.kt
│       │   ├── LoginResponse.kt
│       │   ├── CreateUserRequest.kt
│       │   ├── UpdateUserRequest.kt
│       │   └── UserResponse.kt
│       └── handler/
│           └── GlobalExceptionHandler.kt # RFC 7807 Problem Details
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│       └── V1__create_users_table.sql
└── src/test/                             # 23 testes
```

## Branch: develop
Última atualização: 2026-07-16T17:24:11Z
