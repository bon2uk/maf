package com.maf.product.service;

import com.maf.common.exception.EntityNotFoundException;
import com.maf.product.dto.ProductFilterRequest;
import com.maf.product.dto.ProductResponse;
import com.maf.product.dto.UpdateProductRequest;
import com.maf.product.entity.Product;
import com.maf.product.model.CurrencyCode;
import com.maf.product.repository.ProductRepository;
import com.maf.product.repository.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product createProduct(String name, String description, UUID userId, BigDecimal price, CurrencyCode currency) {
        Product product = Product.create(name, description, userId, price, currency);
        return productRepository.save(product);
    }

    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product", id));
    }

    public Page<ProductResponse> getProducts(ProductFilterRequest filter, Pageable pageable) {
        Specification<Product> specification = Specification.where(ProductSpecifications.notDeleted())
                .and(ProductSpecifications.hasStatus(filter.status()))
                .and(ProductSpecifications.hasUserId(filter.userId()))
                .and(ProductSpecifications.hasBookedId(filter.bookedId()))
                .and(ProductSpecifications.nameContains(filter.name()))
                .and(ProductSpecifications.priceGreaterThanOrEqualTo(filter.minPrice()))
                .and(ProductSpecifications.priceLessThanOrEqualTo(filter.maxPrice()))
                .and(ProductSpecifications.hasCurrency(filter.currency()));

        return productRepository.findAll(specification, pageable).map(ProductResponse::from);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product updateProduct(UUID id, UpdateProductRequest updateProductRequest) {
        Product product = getProductById(id);
        product.update(updateProductRequest.name(), updateProductRequest.description(), updateProductRequest.imageUrl(), updateProductRequest.price(), updateProductRequest.currency());

        return productRepository.save(product);
    }
}