package com.prj.ecommerce.service;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.dto.request.CreateOrderRequest;
import com.prj.ecommerce.dto.response.CreateOrderListResponse;
import com.prj.ecommerce.dto.response.CreateOrderResponse;

public interface OrderService {
    CreateOrderListResponse getOrders(String keyword, OrderStatus status);
    CreateOrderResponse getOrderItems(Long orderId);
    CreateOrderListResponse createOrder(CreateOrderRequest request);
    CreateOrderResponse cancelOrder(Long orderId);
}
