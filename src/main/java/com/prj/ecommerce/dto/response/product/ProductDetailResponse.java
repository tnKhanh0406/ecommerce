package com.prj.ecommerce.dto.response.product;

import com.prj.ecommerce.dto.response.image.ProductImageResponse;
import com.prj.ecommerce.dto.response.review.ProductReviewResponse;
import com.prj.ecommerce.dto.response.variant.ProductVariantResponse;
import com.prj.ecommerce.dto.response.attribute.ProductAttributeResponse;
import com.prj.ecommerce.dto.response.category.CategoryResponse;
import com.prj.ecommerce.dto.response.shop.ShopResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailResponse {
    // Core
    private Long id;
    private String name;
    private String description;
    private String status;

    // Price
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // Media
    private List<ProductImageResponse> productImages;
    private List<ProductImageResponse> variantImages;

    // Category
    private List<CategoryResponse> breadcrumb;

    // Attribute (Color, Size)
    private List<ProductAttributeResponse> attributes;

    // Variants (ẩn, dùng khi FE chọn attribute)
    private List<ProductVariantResponse> variants;

    // Stats
    private Integer soldCount;
    private BigDecimal rating;
    private Integer reviewCount;

    //Shop
    private ShopResponse shop;

    //Review
    private List<ProductReviewResponse> reviews;
}

