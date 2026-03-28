package com.maf.product.service;

import com.maf.product.dto.UpdateProductRequest;
import com.maf.product.entity.Product;
import com.maf.product.model.CurrencyCode;
import com.maf.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
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

    public Product getProductById(UUID id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
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