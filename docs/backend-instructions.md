# Backend Delivery Playbook

**Mandatory Highlight: ALWAYS consult Context7 MCP before writing any code. Treat all library, API, or framework knowledge as unknown until Context7 MCP confirms it.**

## Activation
- Apply this playbook when touching backend artifacts: any `.java`, `.xml`, or `.properties` file; anything under `src/main/java`, `src/test/java`, or future `backend/` directories; Maven build files (`pom.xml`).
- If a task involves both backend and frontend changes, coordinate with the frontend playbook and run Intent Integrity Chain (IIC) gates for each scope separately.

## Methodology (Intent Integrity Chain)
- Obey IIC phase gates: no implementation starts before analysis, specification, and executable tests are formally approved.
- After generating tests in Phase 3, stop for human approval before any production code.
- Preserve traceability: link requirements → scenarios → tests → commits; tag each completed phase (`phase-{n}-{label}`).

## Stack & Constraints (from requirements)
- Java 25 LTS, Spring Boot 3.5.6, Maven ≥ 3.9.
- REST client operations must use Spring's `RestClient`; avoid `RestTemplate` or extra frameworks.
- SLF4J default logging only; do not add extra observability tooling.
- Records for DTOs; constructor injection; methods ≤ 20 lines; clarity over cleverness.

## Out of Scope — Reject or Escalate
- No WebSockets, circuit breakers, connection pools, databases, Docker, MQTT, authentication, or advanced metrics.
- Do not introduce Redis, Actuator endpoints, rate limiting, or custom logging stacks.

## Delivery Workflow
1. **Analysis:** refine scope in `docs/product_context.md` & `docs/active_context.md`; identify risks.
2. **Specification:** detail backend behaviors in `docs/requirements.md` and enumerate scenarios in `docs/test_scenarios.md`.
3. **Test Construction:** scaffold Maven modules/tests; confirm they fail for correct reasons before code exists; lock tests.
4. **Implementation:** add minimal Spring Boot code to satisfy locked tests; keep suite green.
5. **Hardening:** validate NFRs (graceful bulb failure handling, reconnect logic); document evidence in `docs/tech_assumptions.md`.

## Testing Expectations
- Prefer integration coverage via Playwright/system tests; mock `RestClient` for unit-level checks on `ShellyBulbService` and controllers.
- Ensure tests reflect specification scenarios; no edits after lock without reopening Phase 3.

## Acceptance Reminders
- Application must boot with `mvn spring-boot:run`.
- Bulb color changes within 2 seconds when manually triggered; auto mode fires every 3 seconds.
- Handle offline bulb, camera reconnects, and invalid selection without crashing.

## Collaboration Notes
- Highlight pending human approvals at each phase gate.
- Surface Context7 MCP citations alongside implementation decisions so reviewers can trace source knowledge.
