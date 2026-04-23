package com.maf.product.repository;

import com.maf.product.entity.Product;
import com.maf.product.model.CurrencyCode;
import com.maf.product.model.ProductStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductSpecifications {
    private ProductSpecifications() {}

    public static Specification<Product> notDeleted() {
        return (root, query, cb) -> cb.notEqual(root.get("status"), ProductStatus.DELETED);
    }

    public static Specification<Product> hasStatus(ProductStatus status) {
        return (root, query, cb) -> status == null ? null : cb.notEqual(root.get("status"), status);
    }

    public static Specification<Product> hasUserId(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("userId"), userId);
    }

    public static Specification<Product> hasBookedId(UUID bookedId) {
        return (root, query, cb) -> bookedId == null ? null : cb.equal(root.get("bookedById"), bookedId);
    }

    public static Specification<Product> nameContains(String name) {
        return (root, query, cb) ->  name == null || name.isBlank()
                ? null
                : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Product> priceGreaterThanOrEqualTo(BigDecimal minPrice) {
        return (root, query, cb) ->
                minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceLessThanOrEqualTo(BigDecimal maxPrice) {
        return (root, query, cb) ->
                maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> hasCurrency(CurrencyCode currency) {
        return (root, query, cb) ->
                currency == null ? null : cb.equal(root.get("currency"), currency);
    }
}
