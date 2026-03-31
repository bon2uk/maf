package com.maf.product.controller;

import com.maf.common.entity.CustomUserPrincipal;
import com.maf.product.dto.CreateProductRequest;
import com.maf.product.dto.ProductFilterRequest;
import com.maf.product.dto.ProductResponse;
import com.maf.product.dto.UpdateProductRequest;
import com.maf.product.entity.Product;
import com.maf.product.model.CurrencyCode;
import com.maf.product.model.ProductStatus;
import com.maf.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductsController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ProductResponse.from(productService.getProductById(id)));
    }


    @PostMapping()
    public ResponseEntity<ProductResponse> createProduct(@AuthenticationPrincipal CustomUserPrincipal principal,
        @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(ProductResponse.from(productService.createProduct(
                request.name(),
                request.description(),
                principal.getUserId(),
                request.price(),
                request.currency()
        )));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> updateById(@PathVariable UUID id, @Valid @RequestBody UpdateProductRequest updateProductRequest) {
        return ResponseEntity.ok(ProductResponse.from(productService.updateProduct(id, updateProductRequest)));
    }

    @GetMapping()
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false)ProductStatus productStatus,
            @RequestParam(required = false)UUID userId,
            @RequestParam(required = false)UUID bookedId,
            @RequestParam(required = false)String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) CurrencyCode currency,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ProductFilterRequest filter = new ProductFilterRequest(productStatus, userId, bookedId, name, minPrice, maxPrice, currency);
        if (filter.minPrice() != null && filter.maxPrice() != null
                && filter.minPrice().compareTo(filter.maxPrice()) > 0) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
        return ResponseEntity.ok(productService.getProducts(filter, pageable));
    }

}