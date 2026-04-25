package com.maf.telegram.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Wire-level response from {@code POST /internal/parser/product}. Mirrors
 * {@code com.maf.parser.dto.ParsedProductResponse} on the parser-service
 * side; kept as a local copy to avoid a hard module dependency on the
 * parser-service's classpath.
 *
 * <p>All fields are nullable — the LLM is allowed to leave fields blank
 * when the source message doesn't carry enough information to fill them.
 * {@code currency} and {@code category} are intentionally typed as
 * {@code String} so that an unknown enum value coming back from a future
 * parser-service version doesn't crash the telegram-service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ParsedProductResponse(
        String title,
        String description,
        BigDecimal price,
        String currency,
        String category,
        String model
) {
}
