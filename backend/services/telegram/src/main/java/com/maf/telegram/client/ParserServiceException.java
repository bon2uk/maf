package com.maf.telegram.client;

/**
 * Thrown by {@link ParserClient} when the parser-service is unreachable,
 * returns a non-2xx response, or returns an unparseable body. The caller
 * decides how to translate this into a per-message status update.
 */
public class ParserServiceException extends RuntimeException {

    public ParserServiceException(String message) {
        super(message);
    }

    public ParserServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
