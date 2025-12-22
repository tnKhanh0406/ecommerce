package com.prj.ecommerce.api;

import com.prj.ecommerce.dto.request.CreateShopRequest;
import com.prj.ecommerce.dto.response.CreateShopResponse;
import com.prj.ecommerce.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopApiController {
    private final ShopService shopService;

    @PostMapping
    public ResponseEntity<CreateShopResponse> createShop(@Valid @RequestBody CreateShopRequest shop) {
        CreateShopResponse createShopResponse = shopService.createShop(shop);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createShopResponse);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/{shopId}")
    public ResponseEntity<CreateShopResponse> updateShop(@PathVariable Long shopId, @Valid @RequestBody CreateShopRequest shop) {
        CreateShopResponse createShopResponse = shopService.updateShop(shopId, shop);
        return ResponseEntity.ok(createShopResponse);
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/{shopId}")
    public ResponseEntity<Void> deleteShop(@PathVariable Long shopId) {
        shopService.deleteShop(shopId);
        return ResponseEntity.noContent().build();
    }
}
