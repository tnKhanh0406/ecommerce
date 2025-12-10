package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.AddCartItemRequest;
import com.prj.ecommerce.dto.request.UpdateCartItemRequest;
import com.prj.ecommerce.dto.response.AddCartItemResponse;
import com.prj.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    @GetMapping


    @PostMapping
    public ResponseEntity<AddCartItemResponse> addCartItem(@Valid @RequestBody AddCartItemRequest addCartItemRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cartService.addCartItem(addCartItemRequest));
    }

    @PutMapping
    public ResponseEntity<AddCartItemResponse> updateCartItem(@Valid @RequestBody UpdateCartItemRequest updateCartItemRequest) {
        return ResponseEntity.ok(cartService.updateCartItem(updateCartItemRequest));
    }

    @DeleteMapping("{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartItemId) {
        cartService.deleteCartItem(cartItemId);
        return ResponseEntity.noContent().build();
    }
}
