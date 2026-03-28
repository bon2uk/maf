package com.maf.product.controller;

import com.maf.product.dto.UpdateProductRequest;
import com.maf.product.entity.Product;
import com.maf.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductsController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Product> updateById(@PathVariable UUID id, @Valid @RequestBody UpdateProductRequest updateProductRequest) {
        return ResponseEntity.ok(productService.updateProduct(id, updateProductRequest));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

}