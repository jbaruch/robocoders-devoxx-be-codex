package com.robocoders.vibecoding.service.exception;

public class BulbNotConfiguredException extends RuntimeException {

    public BulbNotConfiguredException() {
        super("Shelly bulb host not configured");
    }
}
