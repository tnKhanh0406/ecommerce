package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.entity.ProductImageEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductImageResponse {
    private Long id;
    private String imageUrl;
    private String imageType;
    private Long productId;
    private Long variantId;
    private Long reviewId;

    public static ProductImageResponse fromEntity(ProductImageEntity productImageEntity) {
        Long reviewId = null;
        if (productImageEntity.getReview() != null) {
            reviewId = productImageEntity.getReview().getId();
        }
        return new ProductImageResponse(
                productImageEntity.getId(),
                productImageEntity.getImageUrl(),
                productImageEntity.getImageType().toString(),
                productImageEntity.getProduct().getId(),
                productImageEntity.getVariant().getId(),
                reviewId
        );
    }
}
