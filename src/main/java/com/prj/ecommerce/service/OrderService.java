package com.prj.ecommerce.service;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.dto.request.CreateOrderRequest;
import com.prj.ecommerce.dto.response.CreateOrderListResponse;
import com.prj.ecommerce.dto.response.CreateOrderResponse;
import com.prj.ecommerce.dto.response.ShopSalesAnalyticsResponse;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    CreateOrderListResponse getOrders(String keyword, OrderStatus status);
    CreateOrderListResponse getOrdersForAdmin(String keyword, OrderStatus status);
    List<CreateOrderResponse> getOrdersByShopId(Long shopId, OrderStatus status);
    CreateOrderResponse getOrderItems(Long orderId);
    CreateOrderResponse getOrderDetailForAdmin(Long orderId);
    CreateOrderListResponse createOrder(CreateOrderRequest request);
    CreateOrderResponse cancelOrder(Long orderId);
    CreateOrderResponse updateOrderStatusBySeller(Long orderId, OrderStatus newStatus);
    CreateOrderResponse updateOrderStatusByAdmin(Long orderId, OrderStatus newStatus);
    ShopSalesAnalyticsResponse getShopSalesAnalytics(Long shopId, LocalDate startDate, LocalDate endDate);
}
