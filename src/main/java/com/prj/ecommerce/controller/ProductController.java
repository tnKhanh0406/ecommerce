package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.CreateProductRequest;
import com.prj.ecommerce.dto.request.UpdateBasicProductRequest;
import com.prj.ecommerce.dto.response.CreateProductResponse;
import com.prj.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<CreateProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @PutMapping("/{productId}/basic")
    public ResponseEntity<CreateProductResponse> updateProduct(@PathVariable Long productId,
                                                               @Valid @RequestBody UpdateBasicProductRequest request) {
        CreateProductResponse createProductResponse = productService.updateBasicProduct(productId, request);
        return ResponseEntity.ok(createProductResponse);
    }
}
