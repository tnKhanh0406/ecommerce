package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.CreateShopRequest;
import com.prj.ecommerce.dto.response.CreateShopResponse;
import com.prj.ecommerce.entity.ShopEntity;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.model.UserPrincipal;
import com.prj.ecommerce.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;

    @PostMapping("/create")
    public ResponseEntity<CreateShopResponse> createShop(@RequestBody CreateShopRequest shop) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity user = userPrincipal.getUserEntity();
        ShopEntity shopEntity = shopService.createShop(user.getId(), shop);
        CreateShopResponse createShopResponse = new CreateShopResponse().toCreateResponse(shopEntity);
        return new ResponseEntity<>(createShopResponse, HttpStatus.CREATED);
    }
}
