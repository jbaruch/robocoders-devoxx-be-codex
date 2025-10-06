package com.robocoders.vibecoding.config;

import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.StringUtils;

@Validated
@ConfigurationProperties(prefix = "shelly.bulb")
public record ShellyProperties(String host) {

    public Optional<String> resolvedHost() {
        if (StringUtils.hasText(host)) {
            return Optional.of(host.trim());
        }
        String envHost = System.getenv("SHELLY_BULB_HOST");
        if (StringUtils.hasText(envHost)) {
            return Optional.of(envHost.trim());
        }
        return Optional.empty();
    }
}
