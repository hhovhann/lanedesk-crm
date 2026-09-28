# LaneDesk CRM

Single-user CRM for a US freight sales agent. Spec: `docs/PRD.md` (source of truth). Currently **Phase 1 only** — no AI code until the owner says "start phase 2".

## Commands
- Start DB: `export LANEDESK_DB_PASSWORD=...; docker compose up -d`
- Run app: `LANEDESK_DB_PASSWORD=... LANEDESK_USER=... LANEDESK_PASSWORD=... ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` (`dev` loads seed data)
- Test: `./mvnw verify` (Testcontainers starts its own Postgres; Docker must be running)

## Stack
Java 27, Spring Boot 4.1.1, Spring Data JPA, Flyway, PostgreSQL, Thymeleaf + htmx, Spring Security (one user from env vars). Fall back to Java 25 only if a dependency breaks on 27.

## Conventions
- Exactly 4 tables: `company`, `contact`, `activity`, `shipment`. No extra tables, no SPA, no Kafka/Redis.
- Schema is owned by Flyway (`db/migration`); seed data in `db/seed` (dev profile only). `ddl-auto: validate`.
- Package by feature under `com.lanedesk` (company, contact, activity, shipment, today, stats, config).
- All timestamps `timestamptz`, stored UTC; render in the contact's or agent's zone.
- Settings (commission, targets, agent zone) live in `application.yml`, not in a table.
- Secrets only from env vars. Never commit `.env`.
- Logging a call must stay ≤ 3 clicks.
- Per feature: tests, `./mvnw verify`, then commit.
