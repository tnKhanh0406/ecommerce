package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.CreateShopRequest;
import com.prj.ecommerce.entity.ShopEntity;

public interface ShopService {
    ShopEntity createShop(Long userId, CreateShopRequest createShopRequest);
}
