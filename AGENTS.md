# Agent Operations Guide

**Mandatory Highlight:** ALWAYS consult Context7 MCP before proposing or writing any code. Treat every library, API, or platform detail as unknown until Context7 MCP confirms it. Do not rely on pre-trained memory, external searches, or assumptions.

## Intent Integrity Chain at a Glance
- Work in gated phases: analysis → specification → test construction → implementation → hardening. Never skip gates.
- Halt after test generation (Phase 3) for human approval before implementation begins.
- Maintain traceability across artifacts—requirements → scenarios → tests → commits—and tag each phase completion (`phase-{n}-{label}`).

## Activation Rules
- **Backend scope:** `.java`, `.xml`, `.properties`, `pom.xml`, `src/main/java`, `src/test/java`, or any future `backend/` modules.
- **Frontend scope:** `.html`, `.js`, `.mjs`, `.css`, `.json`, `frontend/`, `public/`, or `src/main/resources/static/`.
- Mixed tasks must satisfy both scopes’ gates; coordinate approvals accordingly.

## Backend Expectations
- Stack: Java 25 LTS, Spring Boot 3.5.6, Maven ≥ 3.9; use Spring `RestClient`, records for DTOs, constructor injection.
- Out of scope: WebSockets, circuit breakers, databases, Docker, MQTT, advanced metrics/logging, authentication, Redis, rate limiting.
- Tests: prioritize integration and targeted unit tests (`ShellyBulbService`, `ColorController`); lock tests before coding.
- Acceptance: app runs via `mvn spring-boot:run`, manual color sends update bulb within 2 seconds, auto mode fires every 3 seconds, handles offline bulb/camera gracefully.

## Frontend Expectations
- Stack: Vanilla HTML5, ES2024 JavaScript, Color Thief 2.4.0, modern CSS (Grid/Flexbox); no frameworks or build tools.
- Out of scope: React/Vue/Angular, Sass/Less, WebSockets, OffscreenCanvas, Web Workers, K-means, HSL/HSV conversions, Kelvin math, advanced MediaStream constraints.
- Tests: Playwright coverage for camera dropdown, live video, color preview cadence, manual/auto send, error messaging, Chrome/Firefox/Safari checks; lock tests before UI code.
- Acceptance: zero-config UX, touch targets ≥ 50px, high contrast, auto camera reconnect, no crashes on invalid selection.

## Collaboration & Documentation
- Reflect decisions in `docs/product_context.md`, `docs/active_context.md`, `docs/tech_assumptions.md`, and scenario mappings in `docs/test_scenarios.md`.
- Cite Context7 MCP sources alongside technical recommendations to simplify human review.
- Flag blockers early (e.g., bulb IP, camera permissions) and request guidance before violating constraints.
