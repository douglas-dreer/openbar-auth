# OPENBAR

Plataforma integrada de gestão para bares e restaurantes.
Arquitetura de microsserviços — cada serviço é independente, escalável e responsável por um domínio do negócio.

## Visão Geral

| Microsserviço | Porta | Função |
|---|---|---|
| `openbar-auth` | 8081 | Identidade e acesso — JWT, CRUD de usuários |
| `openbar-waiter` | 8082 | Sessões de mesa, pedidos, itens |
| `openbar-kitchen` | 8083 | KDS para comida |
| `openbar-counter` | 8084 | KDS para bebidas |
| `openbar-pdv` | 8085 | Billing e pagamentos |
| `openbar-finances` | 8086 | Ledger de turnos |

## Stack

- **Linguagem:** Kotlin 2.1.10 (JVM 21)
- **Framework:** Spring Boot 3.4.7
- **Build:** Gradle 8.14 (Kotlin DSL)
- **Banco:** PostgreSQL 16+ via Spring Data JPA / Hibernate
- **Migrações:** Flyway
- **Messaging:** Apache Kafka
- **Cache/Filas:** Redis
- **CI/CD:** GitHub Actions
- **Qualidade:** SonarQube + JaCoCo + Detekt
- **Docs:** Swagger/OpenAPI (springdoc-openapi)

## Microsserviços

### openbar-auth (v0.1.0)

Serviço de identidade e acesso. Autenticação via JWT e CRUD de usuários.

**Endpoints:**

| Método | Path | Descrição |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Autenticar — retorna JWT |
| `GET` | `/api/v1/auth/users` | Listar usuários (paginado) |
| `GET` | `/api/v1/auth/users/{id}` | Buscar por ID |
| `POST` | `/api/v1/auth/users` | Criar usuário |
| `PUT` | `/api/v1/auth/users/{id}` | Atualizar |
| `DELETE` | `/api/v1/auth/users/{id}` | Soft delete |

**Roles:**

| Role | Descrição |
|---|---|
| `ADMIN` | Acesso total |
| `MANAGER` | Gerente de operações |
| `WAITER` | Garçom |
| `CASHIER` | Operador de caixa |
| `KITCHEN` | Cozinheiro/barman |

Mais detalhes: [`openbar-auth/docs/README.md`](openbar-auth/docs/README.md)

## Início Rápido

### Pré-requisitos

- Java 21+
- Docker + Docker Compose
- Git

### Clone e Setup

```bash
git clone https://github.com/douglas-dreer/openbar-auth.git
cd openbar-auth
cp openbar-auth/.env.example openbar-auth/.env  # configure suas credenciais
docker compose up -d
```

### Build & Run

```bash
# Build completo (com testes)
./gradlew build

# Rodar openbar-auth
./gradlew :openbar-auth:bootRun
```

O serviço inicia na porta **8081**.

### Testes

```bash
# Todos os testes
./gradlew test

# Cobertura
./gradlew :openbar-auth:test :openbar-auth:jacocoTestReport

# Análise estática
detekt --input openbar-auth/src/main/kotlin
```

## Arquitetura

```
openbar-project/
├── openbar-auth/           # Microsserviço de autenticação
│   ├── src/main/kotlin/    # Código fonte
│   ├── src/test/kotlin/    # Testes (42 testes)
│   ├── sonar-project.properties
│   ├── Dockerfile
│   └── .env
├── openbar-waiter/         # (a implementar)
├── openbar-kitchen/        # (a implementar)
├── openbar-counter/        # (a implementar)
├── openbar-pdv/            # (a implementar)
├── openbar-finances/       # (a implementar)
├── .github/workflows/      # CI/CD pipelines
├── build.gradle.kts        # Build raiz
├── CHANGELOG.md
└── docker-compose.yml
```

## CI/CD

### Pipelines

| Pipeline | Trigger | Função |
|---|---|---|
| CI Pipeline | push/PR → develop, main | Build, test, SonarQube, Docker |
| Release | tag v* | Cria GitHub Release com artefatos |

### Quality Gates

| Métrica | Threshold | Atual |
|---|---|---|
| Coverage | > 80% | 94.4% |
| Bugs | 0 | 0 |
| Vulnerabilities | 0 | 0 |
| Code Smells | 0 | 0 |
| Duplications | < 3% | 0% |

### Branch Protection

- `main` — requer PR de `develop`, status checks (`Build & Test` + `Code Quality`), 1 approval

## Kafka Topics

| Topic | Producer | Consumer(s) |
|---|---|---|
| `order.items_added` | openbar-waiter | openbar-kitchen, openbar-counter |
| `order.item_canceled` | openbar-waiter | openbar-kitchen, openbar-counter, openbar-pdv |
| `invoice.payment_received` | openbar-pdv | openbar-finances |
| `table.freed` | openbar-pdv | openbar-waiter |

## Changelog

Ver [`CHANGELOG.md`](CHANGELOG.md) para histórico de versões.

## Licença

Apache License 2.0
