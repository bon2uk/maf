package com.maf.product.repository;

import com.maf.product.entity.Product;
import com.maf.product.model.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findAllByStatus(ProductStatus status);

    List<Product> findAllByUserId(UUID userId);

    List<Product> findAllByBookedById(UUID bookedById);

    Optional<Product> findByIdAndStatusNot(UUID id, ProductStatus status);
}