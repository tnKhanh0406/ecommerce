package com.prj.ecommerce.controller;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.dto.request.CreateOrderRequest;
import com.prj.ecommerce.dto.response.CreateOrderListResponse;
import com.prj.ecommerce.dto.response.CreateOrderResponse;
import com.prj.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public CreateOrderListResponse getOrders(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) OrderStatus status) {
        return orderService.getOrders(keyword, status);
    }

    @GetMapping("/{orderId}")
    public CreateOrderResponse getOrder(@PathVariable Long orderId) {
        return orderService.getOrderItems(orderId);
    }

    @PostMapping
    public ResponseEntity<CreateOrderListResponse> createOrderList(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<CreateOrderResponse> updateOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}
