package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.CreateOrderRequest;
import com.prj.ecommerce.dto.response.CreateOrderListResponse;

public interface OrderService {
    CreateOrderListResponse createOrder(CreateOrderRequest request);
}
