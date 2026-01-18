package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.entity.OrderItemEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderItemResponse {
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

    public static CreateOrderItemResponse fromEntity(OrderItemEntity entity) {
        ProductReviewResponse review =
                entity.getReview() != null
                        ? ProductReviewResponse.fromEntity(entity.getReview())
                        : null;
        return new CreateOrderItemResponse(
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
