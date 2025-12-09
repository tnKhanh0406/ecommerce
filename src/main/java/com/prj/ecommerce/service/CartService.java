package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.AddCartItemRequest;
import com.prj.ecommerce.dto.response.AddCartItemResponse;

public interface CartService {
    AddCartItemResponse addCartItem(AddCartItemRequest addCartItemRequest);
}
