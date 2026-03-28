package com.maf.product.mapper;


import com.maf.product.dto.ProductResponse;
import com.maf.product.entity.Product;

public class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(), product.getBookedById(),product.getName(), product.getDescription(), product.getStatus(), product.getPrice(), product.getCurrency(), product.getCreatedAt(), product.getUpdatedAt()
        );
    }
}
