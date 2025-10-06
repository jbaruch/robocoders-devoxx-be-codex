# Frontend Delivery Playbook

**Mandatory Highlight: ALWAYS consult Context7 MCP before writing any code. All technical facts (APIs, browser support, libraries) must be fetched from Context7 MCP—never assume or rely on pre-trained knowledge.**

## Activation
- Use this playbook for any `.html`, `.js`, `.css`, `.mjs`, or `.json` assets and for work inside `frontend/`, `public/`, or `src/main/resources/static/` directories.
- When backend changes accompany frontend tasks, coordinate IIC phases with the backend playbook and secure approvals per scope.

## Methodology (Intent Integrity Chain)
- Follow IIC gates: analysis → specification → test construction → implementation → hardening, with mandatory human approval after test generation.
- Document traceability: requirements → scenarios → tests → shipped assets; tag each completed phase (`phase-{n}-{label}`).

## Stack & Constraints (from requirements)
- Vanilla HTML5 + modern ES2024 JavaScript; no build tooling (Webpack, Vite) or frameworks.
- Use Color Thief 2.4.0 for dominant color extraction; confirm API usage with Context7 MCP before coding.
- Styling via modern CSS (Grid/Flexbox). Keep UI touch-friendly (≥ 50px controls) and high contrast.

## Out of Scope — Reject or Escalate
- No frontend frameworks (React/Vue/Angular), preprocessors (Sass/Less), WebSockets, OffscreenCanvas, Web Workers, K-means clusters, HSL/HSV conversions, or Kelvin calculations.
- Do not add advanced MediaStream constraints configuration or extra build steps.

## Delivery Workflow
1. **Analysis:** capture UX intent, accessibility constraints, and demo assumptions in `docs/product_context.md` / `docs/active_context.md`.
2. **Specification:** describe UI behaviors and color-sync flows in `docs/requirements.md`; map user journeys to scenarios in `docs/test_scenarios.md`.
3. **Test Construction:** draft Playwright scenarios for camera selection, color preview updates, manual/auto send flows, error messaging, and cross-browser baseline; lock tests after human approval.
4. **Implementation:** build minimal HTML/CSS/JS aligned with locked tests; keep code simple enough to explain in 5 minutes.
5. **Hardening:** verify usability NFRs (touch targets, contrast), camera reconnect resilience, and demo readiness; update `docs/tech_assumptions.md` with evidence.

## Testing Expectations
- Automate demo-critical behaviors via Playwright (camera dropdown population, stream display, color preview cadence, network error surfaces, Chrome/Firefox/Safari checks).
- Capture manual testing notes only to supplement automated coverage; reopen Phase 3 if behaviors change.

## Acceptance Reminders
- Webcam should activate without manual prompts during demo (handle permissions gracefully).
- Manual send must update bulb color within 2 seconds; auto mode fires every 3 seconds reliably.
- UI must remain readable from the back of the room; maintain graceful degradation if bulb offline.

## Collaboration Notes
- Transparently log Context7 MCP references for any API usage decisions.
- Coordinate camera IP collection early; surface blockers immediately for human input.
