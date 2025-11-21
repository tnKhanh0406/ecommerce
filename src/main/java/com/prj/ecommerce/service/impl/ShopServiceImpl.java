package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.UserRole;
import com.prj.ecommerce.dto.request.CreateShopRequest;
import com.prj.ecommerce.dto.response.CreateShopResponse;
import com.prj.ecommerce.entity.ShopEntity;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.repository.ShopRepository;
import com.prj.ecommerce.repository.UserRepository;
import com.prj.ecommerce.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    private UserEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public CreateShopResponse createShop(CreateShopRequest createShopRequest) {
        UserEntity user = getCurrentUser();
        if (shopRepository.findByUser_Id(user.getId()).isPresent()) {
            throw new RuntimeException("User already owns a shop");
        }

        user.setRole(UserRole.SELLER);
        userRepository.save(user);

        ShopEntity shopEntity = new ShopEntity();
        shopEntity.setUser(user);
        shopEntity.setShopName(createShopRequest.getShopName());
        shopEntity.setDescription(createShopRequest.getDescription());
        shopEntity.setLogoUrl(createShopRequest.getLogoUrl());
        shopRepository.save(shopEntity);
        return new CreateShopResponse().toCreateResponse(shopEntity);
    }

    @Override
    public CreateShopResponse updateShop(Long shopId, CreateShopRequest createShopRequest) {
        UserEntity user = getCurrentUser();
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        if (!shop.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to update this shop");
        }

        shop.setShopName(createShopRequest.getShopName());
        shop.setDescription(createShopRequest.getDescription());
        shop.setLogoUrl(createShopRequest.getLogoUrl());
        shopRepository.save(shop);
        return new CreateShopResponse().toCreateResponse(shop);
    }

    @Override
    public void deleteShop(Long shopId) {
        shopRepository.deleteById(shopId);
    }
}
