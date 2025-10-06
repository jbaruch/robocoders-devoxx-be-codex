package com.robocoders.vibecoding.web;

import com.robocoders.vibecoding.domain.ColorCommand;
import com.robocoders.vibecoding.domain.Mode;
import com.robocoders.vibecoding.domain.RgbwColor;
import com.robocoders.vibecoding.service.ShellyBulbService;
import com.robocoders.vibecoding.web.dto.ColorCommandRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/colors")
public class ColorController {

    private static final int DEFAULT_TRANSITION_MS = 500;

    private final ShellyBulbService shellyBulbService;

    public ColorController(ShellyBulbService shellyBulbService) {
        this.shellyBulbService = shellyBulbService;
    }

    @PostMapping
    public ResponseEntity<Void> postColor(@Valid @RequestBody ColorCommandRequest request) {
        ColorCommand command = toDomain(request);
        shellyBulbService.dispatch(command);
        return ResponseEntity.noContent().build();
    }

    private ColorCommand toDomain(ColorCommandRequest request) {
        Mode mode = Mode.valueOf(request.mode());
        int transition = request.transitionMs() != null ? request.transitionMs() : DEFAULT_TRANSITION_MS;
        Instant timestamp = parseTimestamp(request.sourceTimestamp());
        ColorCommandRequest.RgbwPayload payload = request.color();
        RgbwColor color = new RgbwColor(payload.r(), payload.g(), payload.b(), payload.w());
        return new ColorCommand(mode, color, transition, timestamp);
    }

    private Instant parseTimestamp(String rawTimestamp) {
        try {
            return Instant.parse(rawTimestamp);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid ISO-8601 timestamp", ex);
        }
    }
}
