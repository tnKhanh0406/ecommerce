package com.prj.ecommerce.api;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.dto.request.order.CreateOrderRequest;
import com.prj.ecommerce.dto.response.order.OrderListResponse;
import com.prj.ecommerce.dto.response.order.OrderResponse;
import com.prj.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderService orderService;

    @GetMapping
    public OrderListResponse getOrders(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) OrderStatus status) {
        return orderService.getOrders(keyword, status);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable Long orderId) {
        return orderService.getOrderItems(orderId);
    }

    @PostMapping
    public ResponseEntity<OrderListResponse> createOrderList(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}
