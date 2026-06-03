package com.prj.ecommerce.service;

import com.prj.ecommerce.common.Status;
import com.prj.ecommerce.dto.request.attribute.UpdateAttributeRequest;
import com.prj.ecommerce.dto.request.product.CreateProductRequest;
import com.prj.ecommerce.dto.request.product.ProductFilterRequest;
import com.prj.ecommerce.dto.request.product.UpdateBasicProductRequest;
import com.prj.ecommerce.dto.request.variant.ProductVariantListRequest;
import com.prj.ecommerce.dto.response.product.AdminProductResponse;
import com.prj.ecommerce.dto.response.product.CreateProductResponse;
import com.prj.ecommerce.dto.response.product.ProductDetailResponse;
import com.prj.ecommerce.dto.response.variant.ProductVariantListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProductService {
    Page<CreateProductResponse> getProducts(ProductFilterRequest request);
    List<CreateProductResponse> getRecommendProducts();
    ProductDetailResponse getProductDetail(Long id);
    void createProduct(CreateProductRequest request,
                                        List<MultipartFile> productImages,
                                        Map<String, List<MultipartFile>> variantImageMap);
    void updateBasicProduct(Long productId,
                            UpdateBasicProductRequest request,
                            List<MultipartFile> productImages,
                            List<String> existingProductImageUrls);
    void updateBasicProductVariant(Long productId,
                                   ProductVariantListRequest request,
                                   Map<String, List<MultipartFile>> variantImageMap,
                                   Map<Integer, List<String>> existingVariantImageUrls);
    void updateAttribute(Long productId,
                         UpdateAttributeRequest updateAttributeRequest,
                         Map<String, List<MultipartFile>> variantImageMap);
    void deleteProduct(Long productId);
    Page<CreateProductResponse> getProductsByShopId(Long shopId, int page, int size);
    ProductDetailResponse getProductForEdit(Long productId);
    List<Long> getProductCategoryIds(Long productId);

    // Admin methods
    Page<AdminProductResponse> getProductsForAdmin(String search, Status status, Pageable pageable);
    ProductDetailResponse getProductDetailForAdmin(Long productId);
    void updateProductStatusForAdmin(Long productId, Status status);
}
