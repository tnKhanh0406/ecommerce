package com.prj.ecommerce.dto.response.review;

import com.prj.ecommerce.dto.response.image.ProductImageResponse;
import com.prj.ecommerce.entity.ProductImageEntity;
import com.prj.ecommerce.entity.ProductReviewEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewResponse {
    private Long productReviewId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String variantSnapshot;
    private String productName;
    private String productImageUrl;
    private List<ProductImageResponse> reviewImageUrls;
    private ReviewReplyResponse reply;

    public static ProductReviewResponse fromEntity(ProductReviewEntity entity) {
        return new ProductReviewResponse(
                entity.getId(),
                entity.getRating(),
                entity.getComment(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVariantSnapshot(),
                entity.getProduct().getName(),
                entity.getProduct().getThumbnailImg(),
                entity.getImages().stream().map(ProductImageResponse::fromEntity).toList(),
                ReviewReplyResponse.fromEntity(entity.getReply())
        );
    }

    public static ProductReviewResponse fromEntity(
            ProductReviewEntity entity,
            List<ProductImageEntity> images
    ) {
        return new ProductReviewResponse(
                entity.getId(),
                entity.getRating(),
                entity.getComment(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVariantSnapshot(),
                entity.getProduct().getName(),
                null,
                images.stream()
                        .map(ProductImageResponse::fromEntity)
                        .toList(),
                ReviewReplyResponse.fromEntity(entity.getReply())
        );
    }
}
