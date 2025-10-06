package com.robocoders.vibecoding.domain;

import java.time.Instant;

public record ColorCommand(Mode mode, RgbwColor color, int transitionMs, Instant sourceTimestamp) {
}
