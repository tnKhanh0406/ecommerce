package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.*;
import com.prj.ecommerce.dto.response.CreateProductResponse;
import com.prj.ecommerce.dto.response.ProductVariantListResponse;
import com.prj.ecommerce.dto.response.ProductVariantResponse;
import com.prj.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public Page<CreateProductResponse> getProducts(@ModelAttribute ProductFilterRequest request) {
        return productService.getProducts(request);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping
    public ResponseEntity<CreateProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/{productId}/basic")
    public ResponseEntity<CreateProductResponse> updateBasicProduct(@PathVariable Long productId,
                                                               @Valid @RequestBody UpdateBasicProductRequest request) {
        CreateProductResponse createProductResponse = productService.updateBasicProduct(productId, request);
        return ResponseEntity.ok(createProductResponse);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/{productId}/variants")
    public ResponseEntity<ProductVariantListResponse> updateBasicProductVariant(@PathVariable Long productId,
                                                                                @Valid @RequestBody ProductVariantListRequest request) {
        ProductVariantListResponse productVariantResponse = productService.updateBasicProductVariant(productId, request);
        return ResponseEntity.ok(productVariantResponse);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/{productId}/attributes")
    public  ResponseEntity<CreateProductResponse> updateAttributes(@PathVariable Long productId,
                                                                   @Valid @RequestBody UpdateAttributeRequest request) {
        CreateProductResponse createProductResponse = productService.updateAttribute(productId, request);
        return ResponseEntity.ok(createProductResponse);
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}
