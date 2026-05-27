package com.prj.ecommerce.dto.response.order;

import com.prj.ecommerce.entity.OrderEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderForShopResponse {
    private Long id;
    private LocalDateTime createdAt;
    private String note;
    private String orderStatus;
    private String paymentStatus;
    private String receiverName;
    private BigDecimal totalPrice;
    private List<OrderItemSummaryForShopResponse> items = new ArrayList<>();
}
