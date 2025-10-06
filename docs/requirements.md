# Vibecoding Demo: RGBW Control App PRD

## Project Overview

**Goal**: Build a web app that uses webcam feed to detect dominant color and control a Shelly Duo GU10 RGBW smart bulb over local network.

**Demo Context**: 3-hours live conference demo using agentic AI IDEs and tools (Windsurf, Junie, Kiro, Claude Code OpenAI Codex)

**Design Philosophy**: Intentionally prioritize simplicity - NO cloud, databases, Docker, or complex infrastructure.

---

## Technology Stack

### Backend
- **Java**: 25 (LTS, released September 2025)
- **Spring Boot**: 3.5.6 (latest stable)
- **Build Tool**: Maven 3.9+

### Frontend
- **Core**: Vanilla HTML5 + Modern JavaScript (ES2024)
- **Color Detection**: Color Thief 2.4.0
- **Styling**: Modern CSS with CSS Grid/Flexbox


## Non-Functional Requirements

### Usability
- Zero configuration for end user
- Large, touch-friendly buttons (50px+ height)
- High contrast for visibility from distance

### Reliability
- Graceful degradation if bulb offline
- Auto-reconnect camera if stream drops
- No crashes on invalid camera selection

---

## Functional Specification - Phase 2 Draft

The following behavioral requirements will drive Phase 3 test construction. IDs are stable anchors for traceability into `docs/test_scenarios.md` and future commits.

### Backend Requirements
- **[BE-1] Color Command Endpoint** - Expose `POST /api/colors` accepting `{"mode":"MANUAL"|"AUTO","color":{"r":0-255,"g":0-255,"b":0-255,"w":0-255 optional},"transitionMs":int,"sourceTimestamp":ISO-8601}`. When `transitionMs` is omitted default to 500ms. The optional `w` channel remains provisional until Shelly RGBW contract is confirmed with Context7 MCP; implementation must gracefully ignore when absent.
- **[BE-2] Bulb Dispatch** - Translate validated payloads into a single HTTP request to the Shelly Duo bulb with timeout <= 1.5s and retry count 0 (no automatic retries). Exact endpoint/body to be confirmed once Context7 MCP provides authoritative Shelly Gen-2 RGBW API details; placeholder logic will be covered by failing tests in Phase 3.
- **[BE-3] Configuration Surface** - Read bulb host/IP from `application.properties` key `shelly.bulb.host`, falling back to environment variable `SHELLY_BULB_HOST`; reject color commands with 503 when no host configured.
- **[BE-4] Error Mapping** - Return `204` on success, `424` when bulb call fails (includes JSON `{ "error":"BULB_OFFLINE" }`), `400` on validation failure, and `503` when backend not configured. All responses complete within 2s to satisfy manual update requirement.
- **[BE-5] Health Endpoint** - Provide lightweight `GET /api/health` returning `200` and JSON structure with `appVersion`, `bulbConfigured` flag, and timestamp; used by frontend to gate UI state.

### Frontend Requirements
- **[FE-1] Camera Setup Flow** - On load, enumerate media devices, auto-select the first available video input, and request a stream via `navigator.mediaDevices.getUserMedia`; reattempt every 5s if permissions denied or stream drops.
- **[FE-2] Color Extraction Loop** - Sample the current video frame at least once every 500ms, compute dominant RGB via Color Thief 2.4.0, and update a preview swatch plus numeric labels (RGB and hex). Sampling cadence slows to every 1.5s when auto mode disabled to conserve CPU.
- **[FE-3] Manual Send** - Provide a button >= 50px tall; on activation send latest color to backend with `mode=MANUAL`, disable button until response received or 2s elapsed, surface success/error toast.
- **[FE-4] Auto Mode** - Toggle-based control that, when enabled, fires color payloads every 3s (+/- 250ms) with `mode=AUTO`. Auto mode pauses when no live stream is available.
- **[FE-5] Status Feedback** - Display distinct UI states for `bulbConfigured=false`, offline bulb (`424`), missing camera, and generic backend failure. Provide retry affordance for network issues.

### Cross-Cutting Requirements
- **[CC-1] Latency Budget** - End-to-end manual send (button press to Shelly response) must complete within 2s under normal connectivity; surface explicit error if backend exceeds this window.
- **[CC-2] Accessibility** - Maintain WCAG AA contrast for primary text/buttons; ensure controls keyboard accessible and labelled for screen readers.
- **[CC-3] Telemetry Light-Touch** - Console log only high-level state transitions (camera obtained, auto mode toggled, bulb response status) to aid demo narration without introducing logging frameworks.
- **[CC-4] Context7 Validation Gap** - Shelly RGBW HTTP contract remains unverified because Context7 MCP lookups (`/websites/shelly-api-docs_shelly_cloud`) returned "documentation not found". Implementation must pause until authoritative guidance is obtained or stakeholder provides alternative documentation.

---

## Out of Scope (DO NOT IMPLEMENT)

The following are explicitly **excluded** to maintain simplicity:

### Backend
- ❌ WebSocket connections
- ❌ Circuit breakers (Resilience4j)
- ❌ Connection pooling configuration
- ❌ Actuator endpoints
- ❌ Prometheus metrics
- ❌ Redis caching
- ❌ Database (PostgreSQL, H2, etc.)
- ❌ Docker containerization
- ❌ MQTT protocol
- ❌ Authentication/Authorization
- ❌ Rate limiting
- ❌ Logging frameworks beyond SLF4J default

### Frontend
- ❌ WebSocket client
- ❌ K-means clustering for color
- ❌ OffscreenCanvas
- ❌ Web Workers
- ❌ HSL/HSV conversions
- ❌ Kelvin temperature calculations
- ❌ Complex color science
- ❌ MediaStream constraints configuration
- ❌ Frontend frameworks (React, Vue, Angular)
- ❌ Build tools (Webpack, Vite)
- ❌ CSS preprocessors (Sass, Less)

---

## Testing Strategy

### Integration Testing via headless browsers (Playwright) Checklist
- [ ] Camera selection dropdown populates
- [ ] Video stream displays from selected camera
- [ ] Color preview updates continuously
- [ ] Manual mode: send button works
- [ ] Auto mode: color sends every 3 seconds
- [ ] Bulb actually changes color
- [ ] Error messages display on network failure
- [ ] App works on Chrome, Firefox, Safari

### Unit Tests (Optional for Demo)
- `ShellyBulbService`: Mock RestClient calls
- `ColorController`: Test request/response mapping

## Acceptance Criteria

**The demo is successful if**:
- ✅ App runs with single `mvn spring-boot:run` command
- ✅ Webcam activates without manual permission prompts
- ✅ Color preview visibly updates in real-time
- ✅ Manual send changes bulb color within 2 seconds
- ✅ Auto mode sends color every 3 seconds reliably
- ✅ UI is large enough to see from back of conference room
- ✅ No crashes or exceptions during demo
- ✅ Code is simple enough to explain in 5 minutes

---

## Additional Notes for AI Agents

### File Generation Order
1. `pom.xml` - Dependencies first
2. `application.properties` - Configuration
3. Model classes - Simple records
4. `ShellyBulbService` - Core logic
5. `ColorController` - REST endpoint
6. `VibeCodingApplication` - Main class
7. `index.html` - UI structure
8. `app.js` - Client logic
9. `styles.css` - Styling

### Common Pitfalls to Avoid
- Don't use `@EnableWebMvc` (breaks Spring Boot auto-config)
- Don't configure `RestTemplate` (use `RestClient` instead)
- Don't add complex exception handling (keep it simple)
- Don't overthink camera selection (basic dropdown is fine)
- Don't optimize prematurely (code clarity > performance)
- Hardcoded bulb IP will probably be wrong, so it will fail until the user provides real IP. ask for IP soon, before testing.

### Code Style Preferences
- Use records for DTOs (ColorRequest, ColorResponse)
- Use constructor injection (no field injection)
- Keep methods under 20 lines
- Use meaningful variable names
- Add comments only for non-obvious logic
