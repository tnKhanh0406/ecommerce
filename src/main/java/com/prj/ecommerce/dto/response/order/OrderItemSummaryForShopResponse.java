package com.prj.ecommerce.dto.response.order;

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
public class OrderItemSummaryForShopResponse {
    private Long orderItemId;
    private Long productId;
    private String imageUrl;
    private BigDecimal price;
    private Integer quantity;
    private String productName;
    private String productVariantName;
    private BigDecimal totalPrice;
}
