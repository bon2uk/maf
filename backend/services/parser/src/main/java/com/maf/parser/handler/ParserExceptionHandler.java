package com.maf.parser.handler;

import com.maf.parser.exception.LlmServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Uniform error shape for the parser service. Keeps responses predictable so
 * calling services can branch on {@code status} instead of parsing message
 * strings.
 */
@Slf4j
@RestControllerAdvice
public class ParserExceptionHandler {

    @ExceptionHandler(LlmServiceException.class)
    public ResponseEntity<Map<String, Object>> handleLlmFailure(LlmServiceException ex) {
        log.error("Upstream llm-service failure", ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.BAD_GATEWAY.value(),
                "error", "LLM_UNAVAILABLE",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getAllErrors().stream()
                .map(err -> err.getDefaultMessage() == null ? err.getObjectName() : err.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "VALIDATION_FAILED",
                "message", detail
        ));
    }
}
