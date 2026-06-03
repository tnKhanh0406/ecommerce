package com.prj.ecommerce.dto.response.order;

import com.prj.ecommerce.dto.response.review.ReviewOrderPageResponse;
import com.prj.ecommerce.entity.OrderItemEntity;
import com.prj.ecommerce.entity.ProductImageEntity;
import com.prj.ecommerce.entity.ProductReviewEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemSummaryResponse {
    private Long orderItemId;
    private Long productId;
    private String imageUrl;
    private BigDecimal price;
    private Integer quantity;
    private String productName;
    private String productVariantName;
    private BigDecimal totalPrice;

    private Boolean reviewed;
    private Boolean canReview;
    private Boolean canUpdate;
    private ReviewOrderPageResponse reviewResponse;

    public static OrderItemSummaryResponse fromEntity(
            OrderItemEntity item,
            ProductReviewEntity review,
            boolean reviewed,
            boolean canReview,
            boolean canUpdate,
            List<ProductImageEntity> reviewImages
    ) {
        return new OrderItemSummaryResponse(
                item.getId(),
                item.getProductId(),
                item.getImageUrl(),
                item.getPrice(),
                item.getQuantity(),
                item.getProductName(),
                item.getProductVariantName(),
                item.getPrice(),
                reviewed,
                canReview,
                canUpdate,
                review != null ? ReviewOrderPageResponse.fromEntity(review, reviewImages) : null
        );
    }
}
