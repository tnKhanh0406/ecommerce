package com.prj.ecommerce.controller;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/user/order")
    public String orderPage(Model model,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false) OrderStatus status) {
        model.addAttribute("orders", orderService.getOrders(keyword, status).getOrders());
        return "order";
    }
}
