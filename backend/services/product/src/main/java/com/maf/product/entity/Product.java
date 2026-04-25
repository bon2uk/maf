package com.maf.product.entity;

import com.maf.product.model.CurrencyCode;
import com.maf.product.model.ProductStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue
    private UUID id;

    @Setter(AccessLevel.NONE)
    @Column(updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 2048)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    @PositiveOrZero
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyCode currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "booked_by")
    private UUID bookedById;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "booked_at")
    private Instant bookedAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "source_message_id", updatable = false, unique = true)
    private UUID sourceMessageId;

    @Column(name = "category")
    private String category;

    @Setter(AccessLevel.NONE)
    @Column(name = "parser_model", updatable = false)
    private String parserModel;

    @Version
    private Long version;

    public static Product create(String name, String description, UUID userId, BigDecimal price, CurrencyCode currency) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be blank");
        }
        return Product.builder()
                .name(name)
                .description(description)
                .status(ProductStatus.ACTIVE)
                .userId(userId)
                .price(price)
                .currency(currency)
                .build();
    }

    public static Product createDraft(UUID sourceMessageId,
                                      String name,
                                      String description,
                                      BigDecimal price,
                                      CurrencyCode currency,
                                      String category,
                                      String parserModel) {
        if (sourceMessageId == null) {
            throw new IllegalArgumentException("sourceMessageId is required for drafts");
        }
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be blank");
        }
        return Product.builder()
                .name(name)
                .description(description)
                .status(ProductStatus.DRAFT)
                .price(price)
                .currency(currency)
                .sourceMessageId(sourceMessageId)
                .category(category)
                .parserModel(parserModel)
                .build();
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public void update(
            String name,
            String description,
            String imageUrl,
            BigDecimal price,
            CurrencyCode currency
    ) {
        requireNotDeleted();

        if (name != null) {
            validateName(name);
            this.name = name;
        }

        if (description != null) {
            this.description = description;
        }

        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }

        if (price != null) {
            validatePrice(price);
            this.price = price;
        }

        if (currency != null) {
            this.currency = currency;
        }
    }

    public void book(UUID bookedById) {
        requireNotDeleted();
        requireActive();
        requireValidBooker(bookedById);

        this.status = ProductStatus.BOOKED;
        this.bookedById = bookedById;
        this.bookedAt = Instant.now();
    }

    public void unBook() {
        requireNotDeleted();
        if (this.status != ProductStatus.BOOKED) {
            throw new IllegalStateException("Product is not booked");
        }
        this.bookedById = null;
        this.bookedAt = null;
        this.status = ProductStatus.ACTIVE;
    }

    public void markAsDeleted() {
        requireNotDeleted();
        this.status = ProductStatus.DELETED;
        this.deletedAt = Instant.now();
        this.bookedById = null;
        this.bookedAt = null;
    }

    private void requireActive() {
        if (this.status != ProductStatus.ACTIVE) {
            throw new IllegalStateException("Only active product can be booked");
        }
    }

    private void requireNotDeleted() {
        if (this.status == ProductStatus.DELETED) {
            throw new IllegalStateException("Deleted product cannot be modified");
        }
    }

    private void requireValidBooker(UUID bookedById) {
        if (bookedById == null) {
            throw new IllegalArgumentException("Booker id cannot be null");
        }
        if (bookedById.equals(this.userId)) {
            throw new IllegalStateException("Owner cannot book own product");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
    }

}
