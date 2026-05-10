package com.prj.ecommerce.service;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.dto.request.order.CreateOrderRequest;
import com.prj.ecommerce.dto.response.order.OrderListResponse;
import com.prj.ecommerce.dto.response.order.OrderResponse;
import com.prj.ecommerce.dto.response.shop.ShopSalesAnalyticsResponse;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    OrderListResponse getOrders(String keyword, OrderStatus status);
    OrderListResponse getOrdersForAdmin(String keyword, OrderStatus status);
    List<OrderResponse> getOrdersByShopId(Long shopId, OrderStatus status);
    OrderResponse getOrderItems(Long orderId);
    OrderResponse getOrderDetailForAdmin(Long orderId);
    OrderListResponse createOrder(CreateOrderRequest request);
    OrderResponse cancelOrder(Long orderId);
    void updateOrderStatusBySeller(Long orderId, OrderStatus newStatus);
    void updateOrderStatusByAdmin(Long orderId, OrderStatus newStatus);
    ShopSalesAnalyticsResponse getShopSalesAnalytics(Long shopId, LocalDate startDate, LocalDate endDate);
}
