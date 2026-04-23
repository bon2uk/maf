package com.maf.telegram.parsing;

import java.math.BigDecimal;

public record ParsedProductCandidate (
    String title,
    String description,
    BigDecimal price,
    String currency
) {}
