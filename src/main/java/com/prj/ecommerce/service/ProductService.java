package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.CreateProductRequest;
import com.prj.ecommerce.dto.request.ProductVariantListRequest;
import com.prj.ecommerce.dto.request.UpdateAttributeRequest;
import com.prj.ecommerce.dto.request.UpdateBasicProductRequest;
import com.prj.ecommerce.dto.response.CreateProductResponse;
import com.prj.ecommerce.dto.response.ProductVariantListResponse;

public interface ProductService {
    CreateProductResponse createProduct(CreateProductRequest request);
    CreateProductResponse updateBasicProduct(Long productId, UpdateBasicProductRequest request);
    ProductVariantListResponse updateBasicProductVariant(Long productId, ProductVariantListRequest request);
    CreateProductResponse updateAttribute(Long productId, UpdateAttributeRequest updateAttributeRequest);
    void deleteProduct(Long productId);
}
