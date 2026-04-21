package com.prj.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopSalesAnalyticsResponse {
    private LocalDate startDate;
    private LocalDate endDate;

    private Long totalOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private Long pendingOrders;
    private Long confirmedOrders;
    private Long shippingOrders;

    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private BigDecimal cancelRate;
    private BigDecimal completionRate;

    private List<ShopTopProductResponse> topProducts = new ArrayList<>();
}