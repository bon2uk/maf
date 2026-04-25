package com.maf.parser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maf.parser.model.Category;
import com.maf.parser.model.CurrencyCode;

import java.math.BigDecimal;

/**
 * Wire-level DTO matching the response of the Python llm-service:
 * <pre>{@code { "parsed": {...}, "model": "llama3.2:3b" }}</pre>
 *
 * Lives in the {@code dto} package but is purely internal — never returned
 * to API callers directly; {@link ParsedProductResponse} is the public face.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LlmParseResponse(Parsed parsed, String model) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Parsed(
            String title,
            String description,
            BigDecimal price,
            CurrencyCode currency,
            Category category
    ) {
    }
}
