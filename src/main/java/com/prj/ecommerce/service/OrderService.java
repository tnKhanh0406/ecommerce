package com.prj.ecommerce.service;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.dto.request.CreateOrderRequest;
import com.prj.ecommerce.dto.response.CreateOrderListResponse;
import com.prj.ecommerce.dto.response.CreateOrderResponse;
import com.prj.ecommerce.entity.UserAddressEntity;

public interface OrderService {
    CreateOrderListResponse getOrders(String keyword, OrderStatus status);
    CreateOrderListResponse createOrder(CreateOrderRequest request);
    CreateOrderResponse cancelOrder(Long orderId);
}
