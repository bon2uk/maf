package com.maf.product.dto;

import com.maf.product.model.CurrencyCode;
import com.maf.product.model.ProductStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductFilterRequest(
    ProductStatus status,
    UUID userId,
    UUID bookedId,
    String name,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    CurrencyCode currency
) {

}
