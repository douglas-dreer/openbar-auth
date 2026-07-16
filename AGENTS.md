# AGENTS.md

## Project Overview

**OPENBAR** — Integrated bar/restaurant management platform. Microservice architecture, currently greenfield (service modules are empty, awaiting implementation).

Full architecture spec: `documentation/project.md`

## Stack (Mandatory)

- **Language:** Kotlin 2.4.10 (JVM target: Java 25)
- **Framework:** Spring Boot 4.1.0 (WebFlux for async, MVC for sync REST)
- **Build:** Gradle with Kotlin DSL (`build.gradle.kts`)
- **Database:** PostgreSQL 16+ via Spring Data JPA / Hibernate
- **Migrations:** Flyway (scripts in `db/migration`)
- **Messaging:** Apache Kafka (topics partitioned by tenant/filial)
- **Cache/Queues:** Redis (KDS real-time queues, sessions/tokens)
- **Config:** `.env` injected into `application.yml`. Never hardcode credentials.

## Microservices

| Module | Purpose |
|---|---|
| `openbar-auth` | Identity/access — JWT issuance, user CRUD (roles: ADMIN, MANAGER, WAITER, CASHIER, KITCHEN) |
| `openbar-waiter` | Table sessions, orders, order items — produces `order.items_added`, `order.item_canceled`, consumes `table.freed` |
| `openbar-kitchen` | KDS for food — consumes `order.items_added` (routing=KITCHEN), produces ticket status updates |
| `openbar-counter` | KDS for drinks — same architecture as kitchen, consumes `order.items_added` (routing=COUNTER) |
| `openbar-pdv` | Billing/payments — produces `invoice.payment_received`, `table.freed`; consumes `order.item_canceled` |
| `openbar-finances` | Shift ledger — consumes `invoice.payment_received` to create LedgerEntry records |

## API Conventions

- RESTful level 2/3 (Richardson maturity)
- All error responses: **RFC 7807 Problem Details** via global `@RestControllerAdvice`
- Auth: JWT (RS256) validated locally per service using public key — no inter-service auth calls
- Pagination: Spring Data `Pageable` on all list endpoints
- Resilience4j Circuit Breaker on synchronous inter-service calls
- Base path: `/api/v1/...`

## Kafka Topics

| Topic | Producer | Consumer(s) |
|---|---|---|
| `order.items_added` | openbar-waiter | openbar-kitchen, openbar-counter |
| `order.item_canceled` | openbar-waiter | openbar-kitchen, openbar-counter, openbar-pdv |
| `invoice.payment_received` | openbar-pdv | openbar-finances |
| `table.freed` | openbar-pdv | openbar-waiter |

Consumer groups: `kds-group` (kitchen/counter), `finance-group`, `waiter-group`, `pdv-group`.

## Important Gotchas

- `.env` is already in the repo root — do not commit real secrets. `.env-exemple` is the template.
- There is **no build system yet** — no `build.gradle.kts`, no wrapper, no CI. When scaffolding a new service, create the Gradle wrapper and `build.gradle.kts` first.
- All service directories (`openbar-auth/`, `openbar-waiter/`, etc.) are currently empty.
- KDS modules (kitchen/counter) share the same architecture — differ only in consumed Kafka topic routing and consumer group.
- Flyway migration scripts must go in `db/migration` within each service.
- JWT validation is stateless per-service (RS256 public key) — never call openbar-auth to validate tokens at runtime.
