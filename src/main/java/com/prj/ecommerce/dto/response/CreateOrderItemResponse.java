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
    private String imageUrl;
    private BigDecimal price;
    private Integer quantity;
    private String productName;
    private String productVariantName;
    private BigDecimal totalPrice;

    public static CreateOrderItemResponse fromEntity(OrderItemEntity entity) {
        return new CreateOrderItemResponse(
                entity.getId(),
                entity.getImageUrl(),
                entity.getPrice(),
                entity.getQuantity(),
                entity.getProductName(),
                entity.getProductVariantName(),
                entity.getTotalPrice()
        );
    }
}
