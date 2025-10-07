# Product Context — RGBW Control App

## Intent & Goals
- Deliver a conference-demo web application that reads webcam imagery, derives the dominant color, and commands a Shelly Duo GU10 RGBW bulb on the local network to match it.
- Showcase agentic AI IDE workflows by executing the Intent Integrity Chain (IIC) end-to-end inside a three-hour live session.

## Primary Users & Stakeholders
- Demo presenter operating the app on stage.
- Audience observing the live color sync between webcam feed and bulb.
- Reviewers ensuring agent adherence to IIC gates and documented constraints.

## Success Criteria
- Single-command startup via `mvn spring-boot:run`.
- Webcam activates without manual prompts; UI reflects live color updates.
- Manual send updates bulb within 2 seconds; auto mode fires every 3 seconds.
- UI remains readable from distance with touch-friendly controls (≥ 50px).
- No crashes when bulb offline or camera selection invalid; automatic camera reconnect supported.

## Constraints & Assumptions
- Hard requirement to use Java 25 LTS, Spring Boot 3.5.6, Maven ≥ 3.9 on backend; vanilla HTML5 + ES2024, Color Thief 2.4.0, modern CSS on frontend.
- Absolutely avoid out-of-scope features defined in `docs/requirements.md` (no WebSockets, databases, Docker, extra tooling, etc.).
- All technical details must be validated via Context7 MCP prior to implementation.
- Bulb simulator address provided by stakeholders: http://localhost:8080 (override via configuration).

## Open Questions / Risks
- Actual Shelly bulb IP and network reachability for demo environment.
- Browser permission handling for camera in conference conditions; fallback plan if blocked.
- Performance of Color Thief processing in real-time on demo hardware.
