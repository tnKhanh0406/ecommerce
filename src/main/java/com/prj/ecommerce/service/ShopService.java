package com.prj.ecommerce.service;

import com.prj.ecommerce.common.Status;
import com.prj.ecommerce.dto.request.CreateShopRequest;
import com.prj.ecommerce.dto.request.UpdateShopRequest;
import com.prj.ecommerce.dto.response.CreateShopResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ShopService {
    CreateShopResponse getShopById(Long shopId);
    CreateShopResponse getCurrentUserShop();
    CreateShopResponse createShop(CreateShopRequest createShopRequest, MultipartFile image);
    CreateShopResponse updateShop(Long shopId, UpdateShopRequest updateShopRequest, MultipartFile image);
    void deleteShop(Long shopId);

    UpdateShopRequest getUpdateShopRequest(Long shopId);
    
    // Admin methods
    Page<CreateShopResponse> getAllShops(String search, Status status, Pageable pageable);
    CreateShopResponse getShopDetailForAdmin(Long shopId);
    void approveShop(Long shopId);
    void rejectShop(Long shopId);
}
