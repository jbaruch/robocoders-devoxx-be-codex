# System Architecture Outline - Draft

## High-Level Components
- **Frontend (Browser)**: captures webcam stream, extracts dominant RGB color via Color Thief, renders live preview, and issues manual or automatic color commands that satisfy requirements FE-1 through FE-5.
- **Backend (Spring Boot)**: exposes REST API for color updates, validates payloads, and forwards commands to the Shelly Duo bulb through Spring `RestClient`, aligning with BE-1 through BE-5.
- **Shelly Duo Bulb**: receives local network HTTP requests to adjust RGBW output. Contract still pending confirmation via Context7 MCP.

## Deployment Context
- Single host serving static frontend assets from Spring Boot alongside REST endpoints; no external services or cloud dependencies.
- Local network connectivity to Shelly bulb assumed; operator provides host/IP through configuration before runtime.

## API Contracts (Draft)

| Endpoint | Method | Request | Response | Notes |
| --- | --- | --- | --- | --- |
| `/api/colors` | POST | JSON `{"mode":"MANUAL"|"AUTO","color":{"r":0-255,"g":0-255,"b":0-255,"w":0-255 (optional)},"transitionMs":int,"sourceTimestamp":ISO-8601}` | `204` on success, `400` on validation failure, `424` on downstream error, `503` when unconfigured | Maps directly to BE-1 through BE-4. Downstream payload TBD until Shelly docs retrieved via Context7 MCP.
| `/api/health` | GET | none | `200` with `{ "appVersion":string, "bulbConfigured":bool, "timestamp":ISO-8601 }` | Supports BE-5 and FE-5 UI gating.

## Sequence Flows

### Manual Send (CC-1, FE-3)
1. User presses manual send button; frontend disables control and records start timestamp.
2. Frontend posts latest color snapshot to `/api/colors` with `mode=MANUAL`.
3. Backend validates payload, reads bulb host, and invokes Shelly HTTP API via `RestClient` with <= 1.5s timeout.
4. Backend returns status; frontend re-enables button and shows toast within 2s budget.

### Auto Mode Loop (FE-2, FE-4)
1. Auto toggle enabled -> frontend schedules 3s interval.
2. Every cycle: ensure active media stream, compute dominant color, POST to `/api/colors` with `mode=AUTO`.
3. If media stream missing, auto cycle pauses and UI indicates idle state.

### Error Handling (FE-5, BE-4)
- `400` responses surface inline validation error indicating malformed payload.
- `424` responses trigger bulb-offline banner and optional retry control.
- `503` responses highlight missing configuration and disable auto mode until resolved.
- Network failures produce general error state and log minimal diagnostic message.

## Configuration Strategy
- Primary source: `application.properties` key `shelly.bulb.host` (BE-3).
- Override: environment variable `SHELLY_BULB_HOST` for demo convenience.
- Frontend polls `/api/health` on load to determine configuration readiness before enabling controls.

## Outstanding Design Decisions
- Shelly RGBW HTTP request format and authentication model remain unknown pending Context7 MCP documentation (`/websites/shelly-api-docs_shelly_cloud` currently unavailable). Placeholder mocks will stand in Phase 3.
- Strategy for providing deterministic video fixtures in Playwright to satisfy SC-FE-2 and SC-FE-4 timing assertions.
