package com.maf.telegram.client.dto;

/**
 * Wire-level body sent to {@code POST /internal/parser/product}. Mirrors
 * {@code com.maf.parser.dto.ParseMessageRequest} on the parser-service side.
 */
public record ParseProductRequest(String message) {
}
