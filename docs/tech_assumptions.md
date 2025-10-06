# Technical Assumptions & Risks

- Demo hardware provides sufficient CPU/GPU for real-time Color Thief sampling without visible frame drops.
- Local network latency must support ≤2 s round-trip for manual bulb updates; tests enforce this budget ahead of implementation.
- Shelly Duo RGBW HTTP API remains undocumented in Context7 MCP; for now we assume the Gen2 `Light.Set` RPC at `/rpc/Light/0/Set` accepts JSON `{id,on,mode,red,green,blue,white?,brightness,transition_time,source,source_timestamp}`. This must be revisited once official material surfaces.
- Context7 MCP lookups succeeded for Spring Boot RestClient configuration (`/spring-projects/spring-boot`, topic "RestClient") and Color Thief usage (`/lokesh/color-thief`), but Playwright documentation endpoints (`/microsoft/playwright`, `/websites/playwright_dev*`) continue to return "documentation not found".
- Browser autoplay/camera permissions must be pre-granted or handled via friendly retry UI; Playwright suite stubs `navigator.mediaDevices` but needs confirmation this strategy is acceptable for demo hardware.
- Playwright base URL assumes a static server on `http://127.0.0.1:4173`; hosting mechanism (e.g., `npm run serve`) remains TBD and could influence CI orchestration.
