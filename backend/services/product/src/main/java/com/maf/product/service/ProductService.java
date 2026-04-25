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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;


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

    public Product createDraft(UUID sourceMessageId,
                               String name,
                               String description,
                               BigDecimal price,
                               CurrencyCode currency,
                               String category,
                               String parserModel) {
        return productRepository.findBySourceMessageId(sourceMessageId)
                .orElseGet(() -> {
                    Product draft = Product.createDraft(
                            sourceMessageId, name, description, price, currency, category, parserModel);
                    return productRepository.save(draft);
                });
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

    public Product updateProduct(UUID id, UUID requestingUserId, UpdateProductRequest updateProductRequest) {
        Product product = getProductById(id);
        verifyOwnership(product, requestingUserId);
        product.update(updateProductRequest.name(), updateProductRequest.description(), updateProductRequest.imageUrl(), updateProductRequest.price(), updateProductRequest.currency());

        return productRepository.save(product);
    }

    public void deleteProduct(UUID id, UUID requestingUserId) {
        Product product = getProductById(id);
        verifyOwnership(product, requestingUserId);
        product.markAsDeleted();
        productRepository.save(product);
    }

    public Product bookProduct(UUID id, UUID bookerId) {
        Product product = getProductById(id);
        product.book(bookerId);
        return productRepository.save(product);
    }

    public Product unbookProduct(UUID id, UUID requestingUserId) {
        Product product = getProductById(id);
        verifyCanUnbook(product, requestingUserId);
        product.unBook();
        return productRepository.save(product);
    }

    private void verifyOwnership(Product product, UUID requestingUserId) {
        if (!product.getUserId().equals(requestingUserId)) {
            throw new AccessDeniedException("You can only modify your own products");
        }
    }

    private void verifyCanUnbook(Product product, UUID requestingUserId) {
        boolean isOwner = product.getUserId().equals(requestingUserId);
        boolean isBooker = requestingUserId.equals(product.getBookedById());
        if (!isOwner && !isBooker) {
            throw new AccessDeniedException("Only the owner or the person who booked can unbook");
        }
    }
}