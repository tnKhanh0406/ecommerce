package com.prj.ecommerce.dto.response.order;

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
public class OrderForAdminResponse {
    private Long id;
    private LocalDateTime createdAt;
    private String orderStatus;
    private String paymentStatus;
    private String receiverPhone;
    private String receiverName;
    private Long shopId;
    private String shopName;
    private BigDecimal totalPrice;
}
