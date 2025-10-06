package com.robocoders.vibecoding.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record ColorCommandRequest(
    @NotBlank
    @Pattern(regexp = "MANUAL|AUTO")
    String mode,
    @NotNull
    @Valid
    RgbwPayload color,
    @PositiveOrZero
    Integer transitionMs,
    @NotBlank
    String sourceTimestamp
) {

    public record RgbwPayload(
        @Min(0)
        @Max(255)
        int r,
        @Min(0)
        @Max(255)
        int g,
        @Min(0)
        @Max(255)
        int b,
        @Min(0)
        @Max(255)
        Integer w
    ) {
    }
}
