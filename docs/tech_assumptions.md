# Technical Assumptions & Risks

- Demo hardware provides sufficient CPU/GPU for real-time Color Thief sampling without visible frame drops.
- Local network latency must support ≤2 s round-trip for manual bulb updates; tests enforce this budget ahead of implementation.
- Shelly Duo RGBW HTTP API guidance obtained via Context7 MCP (`/websites/shelly-api-docs_shelly_cloud`, snippet “Shelly HTTP Endpoint for Color Control”); implementation now targets `GET /color/0` with query parameters (`turn=on`, RGBW channels, `brightness`, `transition`). Further updates required if future hardware deviates from published spec.
- Context7 MCP lookups succeeded for Spring Boot RestClient configuration (`/spring-projects/spring-boot`, topic "RestClient") and Color Thief usage (`/lokesh/color-thief`), but Playwright documentation endpoints (`/microsoft/playwright`, `/websites/playwright_dev*`) continue to return "documentation not found".
- Browser autoplay/camera permissions must be pre-granted or handled via friendly retry UI; Playwright suite stubs `navigator.mediaDevices` but needs confirmation this strategy is acceptable for demo hardware.
- Playwright base URL assumes a static server on `http://127.0.0.1:4173`; hosting mechanism (e.g., `npm run serve`) remains TBD and could influence CI orchestration.
