package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.cart.AddCartItemRequest;
import com.prj.ecommerce.dto.request.cart.UpdateCartItemRequest;
import com.prj.ecommerce.dto.response.cart.AddCartItemResponse;

import java.util.List;

public interface CartService {
    List<AddCartItemResponse> getTop5CartItems();
    List<AddCartItemResponse> getCartItems();
    AddCartItemResponse addCartItem(AddCartItemRequest addCartItemRequest);
    AddCartItemResponse updateCartItem(UpdateCartItemRequest updateCartItemRequest);
    void deleteCartItem(Long cartItemId);
}
