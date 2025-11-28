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

    private Integer totalProducts;

    public static CreateShopResponse fromEntity(ShopEntity shop) {
        return new CreateShopResponse(
                shop.getId(),
                shop.getShopName(),
                shop.getDescription(),
                shop.getLogoUrl(),
                shop.getStatus().toString(),
                shop.getRating(),
                shop.getCreatedAt(),
                shop.getUser().getId(),
                0
        );
    }
}
