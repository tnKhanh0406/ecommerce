package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.Status;
import com.prj.ecommerce.common.UserRole;
import com.prj.ecommerce.dto.request.CreateShopRequest;
import com.prj.ecommerce.dto.request.UpdateShopRequest;
import com.prj.ecommerce.dto.response.CreateShopResponse;
import com.prj.ecommerce.entity.ShopEntity;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.exception.UserAlreadyHasShopException;
import com.prj.ecommerce.repository.ShopRepository;
import com.prj.ecommerce.repository.UserRepository;
import com.prj.ecommerce.service.CloudinaryService;
import com.prj.ecommerce.service.ShopService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    private UserEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public CreateShopResponse getShopById(Long shopId) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));
        return CreateShopResponse.fromEntity(shop);
    }

    @Override
    public CreateShopResponse getCurrentUserShop() {
        UserEntity user = getCurrentUser();
        if (user.getRole() != UserRole.SELLER) {
            throw new AccessDeniedException("Current user is not seller");
        }

        ShopEntity shop = shopRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));
        return CreateShopResponse.fromEntity(shop);
    }

    @Override
    public CreateShopResponse createShop(CreateShopRequest createShopRequest, MultipartFile image) {
        UserEntity user = getCurrentUser();
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
        // Note: Default status is INACTIVE (pending approval) from entity prePersist
        shopRepository.save(shopEntity);
        return CreateShopResponse.fromEntity(shopEntity);
    }

    @Override
    public CreateShopResponse updateShop(Long shopId, UpdateShopRequest updateShopRequest, MultipartFile image) {
        UserEntity user = getCurrentUser();
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));

        if (!shop.getUser().getId().equals(user.getId())) {
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

        return CreateShopResponse.fromEntity(shop);
    }

    @Override
    public void deleteShop(Long shopId) {
        UserEntity user = getCurrentUser();
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));
        if (!shop.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not allowed to update this shop");
        }
        shopRepository.deleteById(shopId);
    }

    @Override
    public UpdateShopRequest getUpdateShopRequest(Long shopId) {
        ShopEntity shopEntity = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));
        return new UpdateShopRequest(shopEntity.getShopName(), shopEntity.getDescription(), shopEntity.getLogoUrl());
    }

    @Override
    public Page<CreateShopResponse> getAllShops(String search, Status status, Pageable pageable) {
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
        return shops.map(CreateShopResponse::fromEntity);
    }

    @Override
    public CreateShopResponse getShopDetailForAdmin(Long shopId) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));
        return CreateShopResponse.fromEntity(shop);
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
}
