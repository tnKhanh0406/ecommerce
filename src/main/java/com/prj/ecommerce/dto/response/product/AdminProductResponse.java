package com.prj.ecommerce.dto.response.product;

import com.prj.ecommerce.entity.ProductEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private String status;
    private Integer soldCount;
    private BigDecimal rating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private String shopName;
    private String ownerName;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    public static AdminProductResponse fromEntity(ProductEntity product,
                                                  String imageUrl,
                                                  BigDecimal minPrice,
                                                  BigDecimal maxPrice) {
        AdminProductResponse response = new AdminProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setImageUrl(imageUrl);
        response.setStatus(product.getStatus() != null ? product.getStatus().toString() : null);
        response.setSoldCount(product.getSoldCount());
        response.setRating(product.getRating());
        response.setReviewCount(product.getReviewCount());
        response.setCreatedAt(product.getCreatedAt());
        response.setShopName(product.getShop() != null ? product.getShop().getShopName() : "");
        response.setOwnerName(product.getShop() != null && product.getShop().getUser() != null
                ? product.getShop().getUser().getFullName() : "");
        response.setMinPrice(minPrice);
        response.setMaxPrice(maxPrice);
        return response;
    }
}
