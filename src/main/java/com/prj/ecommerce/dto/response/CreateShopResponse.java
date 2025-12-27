package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.entity.ShopEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateShopResponse {

    private Long id;
    private String shopName;
    private String description;
    private String logoUrl;
    private String status;
    private Double rating;

    private LocalDateTime createdAt;

    private Long ownerId;

    private long totalProducts = 0;

    public static CreateShopResponse fromEntity(ShopEntity shop) {
        CreateShopResponse response = new CreateShopResponse();
        response.setId(shop.getId());
        response.setShopName(shop.getShopName());
        response.setDescription(shop.getDescription());
        response.setLogoUrl(shop.getLogoUrl());
        response.setStatus(shop.getStatus().toString());
        response.setRating(shop.getRating());
        response.setCreatedAt(shop.getCreatedAt());
        response.setOwnerId(shop.getUser().getId());
        return response;
    }
}
