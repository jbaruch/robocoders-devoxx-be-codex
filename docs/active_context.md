# Active Context - Phase 4 (Implementation)

- Phase 3 (`phase-3-tests-locked`) received stakeholder go-ahead; backend and frontend red tests now drive implementation.
- Context7 MCP lookups refreshed prior to coding: Spring Boot RestClient guidance (`/spring-projects/spring-boot`, topic "RestClient") and Color Thief API usage (`/lokesh/color-thief`) were retrieved successfully; Shelly Duo RGBW HTTP contract remains unavailable (`/websites/shelly-api-docs_shelly_cloud` still returns "documentation not found").
- Stakeholder updated connectivity details: bulb simulator available at `http://localhost:8080`, and Spring Boot service must listen on port `9080`; implementation still targets Shelly Gen2 `Light.Set` RPC at `/rpc/Light/0/Set` until Context7 MCP publishes authoritative docs.
- Backend focus: `POST /api/colors` + `GET /api/health`, Spring `RestClient` with 1.5 s timeouts, validation + error mapping, and configuration fallback to `SHELLY_BULB_HOST`.
- Frontend focus: vanilla HTML/CSS/JS single page app with Color Thief sampling cadence, manual/auto dispatch flows, status messaging, and Playwright-observable test IDs; camera resiliency includes 5 s retry loop on failure.
- Outstanding risks: confirm Shelly RPC schema once documentation becomes available; finalize static serving approach for Playwright base URL `http://127.0.0.1:4173`; validate mocked MediaDevices strategy against demo browsers.
