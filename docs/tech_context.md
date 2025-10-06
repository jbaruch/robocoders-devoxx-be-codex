# Technology Context - Draft

## Backend
- Language: Java 25 LTS.
- Framework: Spring Boot 3.5.6 with Maven >= 3.9 build tooling.
- HTTP client: Spring `RestClient` for outbound bulb calls.
- Logging: default SLF4J setup; no additional observability stacks.

## Frontend
- HTML5 structure with ES2024 JavaScript executed directly in browser (no bundlers).
- Dominant color extraction via Color Thief 2.4.0 (browser version confirmed via Context7 MCP before usage).
- Styling with modern CSS (Flexbox/Grid) to meet accessibility and demo visibility requirements.

## Tooling & Testing
- Integration coverage planned through Playwright scripts targeting Chrome, Firefox, Safari.
- Optional unit tests using JUnit/Mockito for backend service/controller behavior.
- No Docker, databases, or additional infrastructure tooling allowed.

## Configuration
- Bulb IP to be set through simple configuration (e.g., environment variable or property file) provided before testing.
- Camera permissions must be handled through standard browser prompts; no advanced MediaStream constraint tuning.

## Context7 Documentation Status
- Spring Boot `RestClient` and Color Thief 2.4.0 documentation requests via Context7 MCP (`/spring-projects/spring-boot`, `/lokesh/color-thief`) returned "documentation not found"; treat downstream usage details as unverified until a successful lookup is available.
- Shelly Duo RGBW API documentation request (`/websites/shelly-api-docs_shelly_cloud`) also failed, blocking confirmation of payload format and authentication scheme. Implementation must pause at Phase 3 until authoritative material is retrieved or stakeholders provide direct documentation.
