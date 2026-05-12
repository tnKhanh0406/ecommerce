package com.prj.ecommerce.api;

import com.prj.ecommerce.dto.request.shop.CreateShopRequest;
import com.prj.ecommerce.dto.request.shop.UpdateShopRequest;
import com.prj.ecommerce.dto.response.shop.ShopResponse;
import com.prj.ecommerce.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopApiController {
    private final ShopService shopService;

    @PostMapping
    public ResponseEntity<ShopResponse> createShop(@Valid @RequestBody CreateShopRequest shop,
                                                   @RequestParam(value = "image", required = false) MultipartFile image) {
        ShopResponse createShopResponse = shopService.createShop(shop, image);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createShopResponse);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/{shopId}")
    public ResponseEntity<ShopResponse> updateShop(@PathVariable Long shopId, @Valid @RequestBody UpdateShopRequest updateShopRequest,
                                                   @RequestParam(value = "image", required = false) MultipartFile image) {
        ShopResponse createShopResponse = shopService.updateShop(shopId, updateShopRequest, image);
        return ResponseEntity.ok(createShopResponse);
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/{shopId}")
    public ResponseEntity<Void> deleteShop(@PathVariable Long shopId) {
        shopService.deleteShop(shopId);
        return ResponseEntity.noContent().build();
    }
}
