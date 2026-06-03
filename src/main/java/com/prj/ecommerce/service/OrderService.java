package com.prj.ecommerce.service;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.dto.request.order.CreateOrderRequest;
import com.prj.ecommerce.dto.response.order.*;
import com.prj.ecommerce.dto.response.shop.ShopSalesAnalyticsResponse;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    List<OrderSummaryResponse> getOrders(String keyword, OrderStatus status);
    List<OrderForAdminResponse> getOrdersForAdmin(String keyword, OrderStatus status);
    List<OrderForShopResponse> getOrdersForShop(Long shopId, OrderStatus status);
    OrderResponse getOrderItems(Long orderId);
    OrderResponse getOrderDetailForAdmin(Long orderId);
    void createOrder(CreateOrderRequest request);
    void cancelOrder(Long orderId);
    void updateOrderStatusBySeller(Long orderId, OrderStatus newStatus);
    void updateOrderStatusByAdmin(Long orderId, OrderStatus newStatus);
    ShopSalesAnalyticsResponse getShopSalesAnalytics(Long shopId, LocalDate startDate, LocalDate endDate);
}
