package com.prj.ecommerce.dto.response.order;

import com.prj.ecommerce.dto.response.review.ProductReviewResponse;
import com.prj.ecommerce.entity.OrderItemEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
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
    private ProductReviewResponse reviewResponse;

    public static OrderItemResponse fromEntity(OrderItemEntity entity) {
        ProductReviewResponse review =
                entity.getReview() != null
                        ? ProductReviewResponse.fromEntity(entity.getReview())
                        : null;
        return new OrderItemResponse(
                entity.getId(),
                entity.getProductId(),
                entity.getImageUrl(),
                entity.getPrice(),
                entity.getQuantity(),
                entity.getProductName(),
                entity.getProductVariantName(),
                entity.getTotalPrice(),
                false,
                false,
                false,
                review
        );
    }
}
