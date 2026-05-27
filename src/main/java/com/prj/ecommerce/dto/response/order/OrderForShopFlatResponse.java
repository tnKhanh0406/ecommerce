package com.prj.ecommerce.dto.response.order;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.common.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderForShopFlatResponse {
    private Long orderId;
    private LocalDateTime createdAt;
    private String note;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private String receiverName;
    private BigDecimal totalPrice;

    private Long orderItemId;
    private Long productId;
    private String imageUrl;
    private BigDecimal price;
    private Integer quantity;
    private String productName;
    private String productVariantName;
    private BigDecimal itemTotalPrice;
}
