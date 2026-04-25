package com.maf.parser.dto;

import com.maf.parser.model.Category;
import com.maf.parser.model.CurrencyCode;

import java.math.BigDecimal;

/**
 * Public response returned to callers of the parser service. All fields are
 * nullable — the LLM is intentionally allowed to leave fields blank when the
 * source message doesn't contain enough information to fill them.
 */
public record ParsedProductResponse(
        String title,
        String description,
        BigDecimal price,
        CurrencyCode currency,
        Category category,
        String model
) {
}
