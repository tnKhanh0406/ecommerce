package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.CreateShopRequest;
import com.prj.ecommerce.dto.response.CreateShopResponse;

public interface ShopService {
    CreateShopResponse createShop(CreateShopRequest createShopRequest);
    CreateShopResponse updateShop(Long shopId, CreateShopRequest createShopRequest);
    void deleteShop(Long shopId);
}
