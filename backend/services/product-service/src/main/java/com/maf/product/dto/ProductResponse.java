package com.maf.product.dto;

import com.maf.product.entity.Product;
import com.maf.product.model.CurrencyCode;
import com.maf.product.model.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID bookedById,
        String name,
        String description,
        ProductStatus status,
        BigDecimal price,
        CurrencyCode currency,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getBookedById(),
                product.getName(),
                product.getDescription(),
                product.getStatus(),
                product.getPrice(),
                product.getCurrency(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
