package com.prj.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopTopProductResponse {
    private Long productId;
    private String productName;
    private String imageUrl;
    private Long totalQuantity;
    private Long orderCount;
    private BigDecimal totalRevenue;
}