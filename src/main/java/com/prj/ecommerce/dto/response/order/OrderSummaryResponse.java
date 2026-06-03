package com.prj.ecommerce.dto.response.order;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryResponse {
    private Long id;
    private LocalDateTime createdAt;
    private String note;
    private String orderStatus;
    private Long shopId;
    private String shopName;
    private BigDecimal totalPrice;
    List<OrderItemSummaryResponse> items;
}
