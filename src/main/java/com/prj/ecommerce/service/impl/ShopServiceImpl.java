package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.Status;
import com.prj.ecommerce.common.UserRole;
import com.prj.ecommerce.dto.request.shop.CreateShopRequest;
import com.prj.ecommerce.dto.request.shop.UpdateShopRequest;
import com.prj.ecommerce.dto.response.shop.ShopResponse;
import com.prj.ecommerce.entity.ShopEntity;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.exception.UserAlreadyHasShopException;
import com.prj.ecommerce.repository.ShopRepository;
import com.prj.ecommerce.repository.UserRepository;
import com.prj.ecommerce.service.CloudinaryService;
import com.prj.ecommerce.service.ShopService;
import com.prj.ecommerce.utils.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    private Long getCurrentUserId() {
        return SecurityUtil.getCurrentUserId();
    }

    private UserEntity getCurrentUser() {
        return userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public ShopResponse getShopById(Long shopId) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));
        return enrichShopResponse(shop);
    }

    @Override
    public ShopResponse getCurrentUserShop() {
        UserEntity user = getCurrentUser();
        if (user == null || user.getRole() != UserRole.SELLER) {
            throw new AccessDeniedException("Current user is not seller");
        }

        ShopEntity shop = shopRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));
        return ShopResponse.fromEntity(shop);
    }

    @Override
    public ShopResponse createShop(CreateShopRequest createShopRequest, MultipartFile image) {
        UserEntity user = getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("User must be authenticated to create a shop");
        }
        if (shopRepository.findByUser_Id(user.getId()).isPresent()) {
            throw new UserAlreadyHasShopException("User already owns a shop");
        }

        user.setRole(UserRole.SELLER);
        userRepository.save(user);
        String logoUrl = cloudinaryService.uploadImage(image);

        ShopEntity shopEntity = new ShopEntity();
        shopEntity.setUser(user);
        shopEntity.setShopName(createShopRequest.getShopName());
        shopEntity.setDescription(createShopRequest.getDescription());
        shopEntity.setLogoUrl(logoUrl);
        shopRepository.save(shopEntity);
        return enrichShopResponse(shopEntity);
    }

    @Override
    public ShopResponse updateShop(Long shopId, UpdateShopRequest updateShopRequest, MultipartFile image) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));

        if (!shopRepository.existsByIdAndUser_Id(shopId, getCurrentUserId())) {
            throw new AccessDeniedException("You are not allowed to update this shop");
        }

        String logoUrl = shop.getLogoUrl();

        if (image != null && !image.isEmpty()) {
            logoUrl = cloudinaryService.uploadImage(image);
        }

        shop.setShopName(updateShopRequest.getShopName());
        shop.setDescription(updateShopRequest.getDescription());
        shop.setLogoUrl(logoUrl);
        shopRepository.save(shop);

        return enrichShopResponse(shop);
    }

    @Override
    public void deleteShop(Long shopId) {
        if (!shopRepository.existsByIdAndUser_Id(shopId, getCurrentUserId())) {
            throw new AccessDeniedException("You are not allowed to update this shop");
        }
        shopRepository.deleteById(shopId);
    }

    @Override
    public Page<ShopResponse> getAllShops(String search, Status status, Pageable pageable) {
        Page<ShopEntity> shops;
        if (search != null && !search.trim().isEmpty()) {
            if (status != null) {
                shops = shopRepository.searchShopsByStatus(status, search, pageable);
            } else {
                shops = shopRepository.searchShops(search, pageable);
            }
        } else {
            if (status != null) {
                shops = shopRepository.findByStatus(status, pageable);
            } else {
                shops = shopRepository.findAll(pageable);
            }
        }
        return shops.map(this::enrichShopResponse);
    }

    @Override
    public void approveShop(Long shopId) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));
        shop.setStatus(Status.ACTIVE);
        shopRepository.save(shop);
    }

    @Override
    public void rejectShop(Long shopId) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));
        shop.setStatus(Status.BLOCKED);
        shopRepository.save(shop);
    }

    private ShopResponse enrichShopResponse(ShopEntity shop) {
        ShopResponse response = ShopResponse.fromEntity(shop);
        response.setOwnerId(shop.getUser().getId());
        response.setOwnerName(shop.getUser().getFullName());
        response.setOwnerEmail(shop.getUser().getEmail());
        return response;
    }
}
