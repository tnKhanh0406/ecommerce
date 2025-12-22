package com.prj.ecommerce.api;

import com.prj.ecommerce.dto.request.AddCartItemRequest;
import com.prj.ecommerce.dto.request.UpdateCartItemRequest;
import com.prj.ecommerce.dto.response.AddCartItemResponse;
import com.prj.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartApiController {
    private final CartService cartService;

    @GetMapping
    public List<AddCartItemResponse> findAll() {
        return cartService.getCartItems();
    }


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
