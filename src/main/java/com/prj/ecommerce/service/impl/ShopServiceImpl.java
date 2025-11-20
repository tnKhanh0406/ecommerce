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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    @Override
    public ShopEntity createShop(Long userId, CreateShopRequest createShopRequest) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (shopRepository.findByUser_Id(userId).isPresent()) {
            throw new RuntimeException("User already owns a shop");
        }

        user.setRole(UserRole.SELLER);
        userRepository.save(user);

        ShopEntity shopEntity = new ShopEntity();
        shopEntity.setUser(user);
        shopEntity.setShopName(createShopRequest.getShopName());
        shopEntity.setDescription(createShopRequest.getDescription());
        shopEntity.setLogoUrl(createShopRequest.getLogoUrl());
        return shopRepository.save(shopEntity);
    }
}
