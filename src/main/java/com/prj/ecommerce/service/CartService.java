package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.cart.AddCartItemRequest;
import com.prj.ecommerce.dto.request.cart.UpdateCartItemRequest;
import com.prj.ecommerce.dto.response.cart.CartItemResponse;
import com.prj.ecommerce.dto.response.cart.HeaderCartItemResponse;

import java.util.List;

public interface CartService {
    List<HeaderCartItemResponse> getTop5CartItems();
    long getCartItemCount();
    List<CartItemResponse> getCartItems();
    CartItemResponse addCartItem(AddCartItemRequest addCartItemRequest);
    CartItemResponse updateCartItem(UpdateCartItemRequest updateCartItemRequest);
    void deleteCartItem(Long cartItemId);
}
