package com.prj.ecommerce.service;

import com.prj.ecommerce.common.Status;
import com.prj.ecommerce.dto.request.shop.CreateShopRequest;
import com.prj.ecommerce.dto.request.shop.UpdateShopRequest;
import com.prj.ecommerce.dto.response.shop.ShopResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ShopService {
    ShopResponse getShopById(Long shopId);
    ShopResponse getCurrentUserShop();
    ShopResponse createShop(CreateShopRequest createShopRequest, MultipartFile image);
    ShopResponse updateShop(Long shopId, UpdateShopRequest updateShopRequest, MultipartFile image);
    void deleteShop(Long shopId);

    // Admin methods
    Page<ShopResponse> getAllShops(String search, Status status, Pageable pageable);
    void approveShop(Long shopId);
    void rejectShop(Long shopId);
}
