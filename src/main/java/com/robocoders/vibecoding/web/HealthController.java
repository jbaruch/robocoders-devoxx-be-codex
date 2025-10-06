package com.robocoders.vibecoding.web;

import com.robocoders.vibecoding.config.ShellyProperties;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final ShellyProperties properties;
    private final String appVersion;

    public HealthController(ShellyProperties properties,
            @Value("${app.version:0.0.1-SNAPSHOT}") String appVersion) {
        this.properties = properties;
        this.appVersion = appVersion;
    }

    @GetMapping
    public HealthResponse health() {
        boolean configured = properties.resolvedHost().isPresent();
        return new HealthResponse(appVersion, configured, Instant.now().toString());
    }

    public record HealthResponse(String appVersion, boolean bulbConfigured, String timestamp) {
    }
}
