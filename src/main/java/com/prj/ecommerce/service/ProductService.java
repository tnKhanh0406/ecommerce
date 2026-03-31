package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.*;
import com.prj.ecommerce.dto.response.CreateProductResponse;
import com.prj.ecommerce.dto.response.ProductDetailResponse;
import com.prj.ecommerce.dto.response.ProductVariantListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<CreateProductResponse> getProducts(ProductFilterRequest request);
    List<CreateProductResponse> getRecommendProducts();
    ProductDetailResponse getProductDetail(Long id);
    CreateProductResponse createProduct(CreateProductRequest request);
    CreateProductResponse updateBasicProduct(Long productId, UpdateBasicProductRequest request);
    ProductVariantListResponse updateBasicProductVariant(Long productId, ProductVariantListRequest request);
    CreateProductResponse updateAttribute(Long productId, UpdateAttributeRequest updateAttributeRequest);
    void deleteProduct(Long productId);
    Page<CreateProductResponse> getProductsByShopId(Long shopId, int page, int size);
    ProductDetailResponse getProductForEdit(Long productId);
}
