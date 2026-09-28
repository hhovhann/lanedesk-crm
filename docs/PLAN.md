# LaneDesk CRM — Phase 1 plan

## Context
Single-user CRM for a US freight sales agent (spec: PRD pasted in chat, saved to `docs/PRD.md`). Greenfield project at `~/work/lanedesk-crm`. Phase 1 only: no AI until "start phase 2".

## Verified versions (2026-09-28)
- JDK: Corretto 27 is active (`java -version` = 27). Also in SDKMAN: 25, 27 oracle/amzn.
- Spring Boot **4.1.1** (latest GA; 4.2.0-M2 is milestone, skipped). Manages Flyway 12.4.0, Hibernate 7.4.5, PostgreSQL driver 42.7.13.
- Spring AI **2.0.1** (latest GA; 2.1.0-M1 is milestone). Not added in Phase 1; BOM added in Phase 2.
- Testcontainers 2.0.5. Docker 29.8 running.
- Risk: Boot's parent sets `java.version=17`; I override to 27. If any plugin/dependency fails on 27, fall back to 25 LTS and report why. Fallback is decided at first `./mvnw verify`.

## Structure
```
lanedesk-crm/
  docs/PRD.md, CLAUDE.md, compose.yaml, .env.example, .gitignore, pom.xml, mvnw
  src/main/java/com/lanedesk/
    LaneDeskApplication.java
    config/   (SecurityConfig, LaneDeskProperties: commission-pct, targets, agent zone)
    company/  (Company, CompanyRepository, CompanyService, CompanyController, CsvImportService)
    contact/  activity/  shipment/   (entity, repo, service, controller each)
    today/    (TodayController + TodayService: due follow-ups, call list by local hours, in-transit)
    stats/    (StatsService, StatsController)
  src/main/resources/
    application.yml, db/migration/V1__schema.sql, V2__seed.sql (or dev-profile seeder)
    templates/ (layout, today, companies, company-detail, pipeline, stats, fragments)
    static/ (htmx.min.js vendored)
  src/test/java/...  (Testcontainers Postgres)
```
Package-by-feature; exactly the 4 tables. Thymeleaf + htmx, Spring Security form login with one user from env vars.

## Task list (each: tests → `./mvnw verify` → commit)
1. Scaffold: pom (Boot 4.1.1, Java 27, web, thymeleaf, data-jpa, security, validation, flyway, postgres, testcontainers), compose.yaml (Postgres), Maven wrapper, .gitignore, CLAUDE.md, docs/PRD.md, git init.
2. Flyway V1 schema: 4 tables, enums, constraints (mc_number unique, weight ≤ 48000, delivery ≥ pickup, lost_reason when LOST, contact needs phone or email), generated `margin`, indexes on follow-up/occurred_at.
3. Entities + repositories + a Testcontainers smoke test.
4. Security: form login, credentials from `LANEDESK_USER` / `LANEDESK_PASSWORD` env vars.
5. Companies: list with filters (type, status, state, equipment), create/edit, DO_NOT_CALL respected everywhere.
6. Company detail + contacts + activity timeline + quick "log call" (htmx, ≤3 clicks; sets next follow-up).
7. Shipments + Pipeline (grouped by status, one-click transitions with validation, lost reason).
8. Today view: due follow-ups sorted by contact local time, call list (contacts in 8–17 local time now), in-transit loads.
9. Stats: calls/day, conversations, quotes, win rate, loads/week, margin, commission vs targets (from yml).
10. CSV import for companies.
11. Seed data: 10 shippers, 3 brokers, 5 carriers, contacts across US zones, activities, shipments in every status (dev profile only).
12. Final: README run notes, list of skipped/assumed items.

## Decisions I'll make unless you object
- Postgres via Docker Compose; app on localhost:8080.
- Seed data via a `dev`-profile Flyway location so prod DB stays clean.
- Times stored UTC (`timestamptz`), rendered in contact zone.
- Business hours for call list = 08:00–17:00 contact local time, Mon–Fri.
- No JPA `ddl-auto`; Flyway is the only schema owner.

## Verification
- `docker compose up -d` then `./mvnw verify` (Testcontainers) green.
- `./mvnw spring-boot:run`, log in, click through Today → log call → quote → win → cover → deliver → Stats.
