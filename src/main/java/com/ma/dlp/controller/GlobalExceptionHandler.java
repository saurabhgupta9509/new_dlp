package com.ma.dlp.controller;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<String> handleAgentError(WebClientResponseException e) {
        log.error("Agent request failed: {} {}", e.getStatusCode(), e.getStatusText());

        // Forward the exact error from Agent
        return ResponseEntity
                .status(e.getStatusCode())
                .body(e.getResponseBodyAsString());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericError(Exception e) {
        log.error("Internal server error", e);
        return ResponseEntity
                .internalServerError()
                .body("{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
    }
}