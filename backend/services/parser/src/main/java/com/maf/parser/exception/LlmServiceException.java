package com.maf.parser.exception;

/**
 * Thrown when the parser cannot obtain a usable response from the
 * upstream llm-service (connect error, timeout, 5xx, or malformed JSON).
 * <p>
 * Mapped to HTTP 502 by the global exception handler so callers can
 * distinguish "downstream unhealthy" from their own bad input.
 */
public class LlmServiceException extends RuntimeException {

    public LlmServiceException(String message) {
        super(message);
    }

    public LlmServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
