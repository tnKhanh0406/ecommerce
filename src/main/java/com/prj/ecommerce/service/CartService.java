package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.cart.AddCartItemRequest;
import com.prj.ecommerce.dto.request.cart.UpdateCartItemRequest;
import com.prj.ecommerce.dto.response.cart.CartItemResponse;

import java.util.List;

public interface CartService {
    List<CartItemResponse> getTop5CartItems();
    List<CartItemResponse> getCartItems();
    CartItemResponse addCartItem(AddCartItemRequest addCartItemRequest);
    CartItemResponse updateCartItem(UpdateCartItemRequest updateCartItemRequest);
    void deleteCartItem(Long cartItemId);
}
