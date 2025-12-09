package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.AddCartItemRequest;
import com.prj.ecommerce.dto.response.AddCartItemResponse;
import com.prj.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<AddCartItemResponse> addCartItem(@RequestBody AddCartItemRequest addCartItemRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cartService.addCartItem(addCartItemRequest));
    }
}
