package com.betterlife.notify.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FcmTokenNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleFcmTokenNotFound(FcmTokenNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateFcmTokenException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateFcmToken(DuplicateFcmTokenException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(FcmTokenDisabledException.class)
    public ResponseEntity<Map<String, String>> handleFcmTokenDisabled(FcmTokenDisabledException ex) {
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({FirebaseCredentialLoadException.class, RedisOperationException.class, FcmSendFailedException.class})
    public ResponseEntity<Map<String, String>> handleServerErrors(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", ex.getMessage()));
    }
}
