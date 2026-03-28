package com.maf.product.dto;

import com.maf.product.model.CurrencyCode;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(

        @Size(max = 100)
        String name,

        @Size(max = 2048)
        String description,

        String imageUrl,

        BigDecimal price,

        CurrencyCode currency

) {}