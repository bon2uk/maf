package com.maf.product.dto;

import com.maf.product.model.CurrencyCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 2048)
        String description,

        @Size(max = 2048)
        String imageUrl,

        @NotNull
        @PositiveOrZero
        BigDecimal price,

        @NotNull
        CurrencyCode currency

) {}