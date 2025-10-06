package com.robocoders.vibecoding.web.error;

import com.robocoders.vibecoding.service.exception.BulbDispatchException;
import com.robocoders.vibecoding.service.exception.BulbNotConfiguredException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final ErrorResponse VALIDATION_ERROR = new ErrorResponse("VALIDATION_FAILED");

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(VALIDATION_ERROR);
    }

    @ExceptionHandler(BulbNotConfiguredException.class)
    public ResponseEntity<ErrorResponse> handleNotConfigured() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponse("BULB_NOT_CONFIGURED"));
    }

    @ExceptionHandler(BulbDispatchException.class)
    public ResponseEntity<ErrorResponse> handleDispatchFailure() {
        return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY)
            .body(new ErrorResponse("BULB_OFFLINE"));
    }
}
