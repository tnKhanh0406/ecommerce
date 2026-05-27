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
public class ReviewOrderPageResponse {
    private Long productReviewId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductImageResponse> reviewImageUrls;
    private ReviewReplyResponse reply;

    public static ReviewOrderPageResponse fromEntity(ProductReviewEntity e, List<ProductImageEntity> images) {
        return new ReviewOrderPageResponse(
                e.getId(),
                e.getRating(),
                e.getComment(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                images.stream().map(ProductImageResponse::fromEntity).toList(),
                ReviewReplyResponse.fromEntity(e.getReply())
        );
    }
}
