# Test Scenarios - Phase 3 Scaffolding

Each scenario maps to the requirements in `docs/requirements.md`. Phase 3 locks executable tests ahead of implementation; all referenced tests currently fail because production code and UI are not yet implemented.

## Backend Scenarios

| Scenario ID | Requirement Links | Description | Test Artifact | Notes |
| --- | --- | --- | --- | --- |
| SC-BE-1 | BE-1, BE-4, CC-1 | `POST /api/colors` with in-range RGBW values returns 204 within 2s and forwards payload to Shelly. | `src/test/java/com/robocoders/vibecoding/api/ColorControllerIT.java` (`postColorCommandDispatchesWithinLatencyBudget`) | Uses `MockWebServer` to capture outbound call; asserts latency budget via `assertTimeoutPreemptively`. Fails now with 404 because endpoint not implemented.
| SC-BE-2 | BE-1, BE-4 | Validation rejects out-of-range channels and missing color object. | `src/test/java/com/robocoders/vibecoding/api/ColorControllerValidationIT.java` | Exercises 400 responses and JSON `{ "error":"VALIDATION_FAILED" }` contract. Currently fails (404) until controller validation exists.
| SC-BE-3 | BE-2, BE-4 | Downstream Shelly failure surfaces as 424 with `{ "error":"BULB_OFFLINE" }`. | `src/test/java/com/robocoders/vibecoding/api/ColorControllerIT.java` (`downstreamFailureSurfacedAsFailedDependency`) | Enqueues 503 response on mock Shelly; asserts 424 propagation. Blocked by missing Shelly API contract from Context7 MCP.
| SC-BE-4 | BE-3 | Missing bulb host yields 503 with `BULB_NOT_CONFIGURED`. | `src/test/java/com/robocoders/vibecoding/api/ColorControllerMissingHostIT.java` | Uses blank property override; expects graceful 503. Fails pre-implementation with 404.
| SC-BE-5 | BE-5 | `GET /api/health` returns version, timestamp, and `bulbConfigured` flag. | `src/test/java/com/robocoders/vibecoding/api/HealthEndpointIT.java` | Asserts JSON envelope. Currently fails due to absent health controller.

## Frontend Scenarios

| Scenario ID | Requirement Links | Description | Test Artifact | Notes |
| --- | --- | --- | --- | --- |
| SC-FE-1 | FE-1 | Camera dropdown populates, auto-selects first device, retries on failure. | `tests/color-flow.spec.ts` (`Camera dropdown auto-selects first device and retries on failure`) | Stubs `navigator.mediaDevices`; fails today because UI not yet built and server unavailable.
| SC-FE-2 | FE-2 | Color preview cadence ≥500 ms in auto mode, slows to 1.5 s when disabled. | `tests/color-flow.spec.ts` (`Color preview updates every 500ms in auto mode and slows when paused`) | Verifies `data-last-update` cadence. Awaiting implementation.
| SC-FE-3 | FE-3, CC-1 | Manual send disables button, enforces 2 s latency, shows toast. | `tests/color-flow.spec.ts` (`Manual send disables button, enforces 2s latency budget, and surfaces response toast`) | Requires toast + latency guard. Currently fails (UI missing).
| SC-FE-4 | FE-4 | Auto mode fires payload ~every 3 s and pauses when stream drops. | `tests/color-flow.spec.ts` (`Auto mode fires payload every ~3 seconds and pauses without video stream`) | Uses route interception to count dispatches; currently failing.
| SC-FE-5 | FE-5 | UI surfaces distinct states for config missing, bulb offline, camera missing, generic errors. | `tests/color-flow.spec.ts` (`Distinct UI states for configuration, bulb offline, camera missing, and generic failures`) | Relies on `data-testid`/copy; failing until UI exists.

## Cross-Cutting Scenarios

| Scenario ID | Requirement Links | Description | Test Artifact | Notes |
| --- | --- | --- | --- | --- |
| SC-CC-1 | CC-1, BE-4, FE-3 | End-to-end manual send completes within 2 s under nominal latency; timeout surfaces error. | `ColorControllerIT#postColorCommandDispatchesWithinLatencyBudget` + `tests/color-flow.spec.ts` manual-send test | Backend and frontend assertions both encode latency budget. Both fail pre-implementation.
| SC-CC-2 | CC-2, FE-3, FE-5 | Controls meet WCAG AA contrast and keyboard access. | `tests/color-flow.spec.ts` (`Controls meet accessibility contrast and keyboard reachability`) | Runs `axe-playwright` and keyboard navigation expectations. Awaiting UI markup/styles.
| SC-CC-3 | CC-3 | Console logging limited to state changes (camera ready, auto toggle, bulb response). | `tests/color-flow.spec.ts` (`Only high-level state transition logs are emitted`) | Captures browser console; currently fails because no UI logging exists.
| SC-CC-4 | CC-4 | Blocker to pause implementation until Shelly RGBW HTTP contract confirmed. | Documentation gate | Shelly API documentation still missing from Context7 MCP; implementation cannot proceed beyond Phase 3.

## Outstanding Dependencies

- Still need authoritative Shelly Duo RGBW HTTP contract; Context7 MCP lookups continue to return "documentation not found" (`/websites/shelly-api-docs_shelly_cloud`). Tests for SC-BE-3 / SC-CC-4 remain provisional.
- Frontend Playwright tests target `http://127.0.0.1:4173`; CI strategy for launching static site pending (likely `npm run serve`).
- Need stakeholder confirmation on acceptable mock camera strategy across Chrome/Firefox/Safari.
