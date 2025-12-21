package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.*;
import com.prj.ecommerce.dto.response.CreateProductResponse;
import com.prj.ecommerce.dto.response.ProductVariantListResponse;
import org.springframework.data.domain.Page;

public interface ProductService {
    Page<CreateProductResponse> getProducts(ProductFilterRequest request);
    CreateProductResponse createProduct(CreateProductRequest request);
    CreateProductResponse updateBasicProduct(Long productId, UpdateBasicProductRequest request);
    ProductVariantListResponse updateBasicProductVariant(Long productId, ProductVariantListRequest request);
    CreateProductResponse updateAttribute(Long productId, UpdateAttributeRequest updateAttributeRequest);
    void deleteProduct(Long productId);
}
