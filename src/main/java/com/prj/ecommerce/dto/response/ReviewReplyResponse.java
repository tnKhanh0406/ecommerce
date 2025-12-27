package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.entity.ReviewReplyEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReplyResponse {
    private ProductReviewResponse productReviewResponse;
    private Long reviewReplyId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewReplyResponse fromEntity(ReviewReplyEntity e) {
        if (e == null) {
            return null;
        }
        return new ReviewReplyResponse(
                ProductReviewResponse.fromEntity(e.getReview()),
                e.getId(),
                e.getContent(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
