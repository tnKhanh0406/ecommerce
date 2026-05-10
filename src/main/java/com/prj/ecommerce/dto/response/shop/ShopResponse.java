package com.prj.ecommerce.dto.response.shop;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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
public class ShopResponse {

    private Long id;
    private String shopName;
    private String description;
    private String logoUrl;
    private String status;
    private Double rating;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private Long ownerId;
    private String ownerName;
    private String ownerEmail;

    private long totalProducts = 0;

    public static ShopResponse fromEntity(ShopEntity shop) {
        ShopResponse response = new ShopResponse();
        response.setId(shop.getId());
        response.setShopName(shop.getShopName());
        response.setDescription(shop.getDescription());
        response.setLogoUrl(shop.getLogoUrl());
        response.setStatus(shop.getStatus().toString());
        response.setRating(shop.getRating());
        response.setCreatedAt(shop.getCreatedAt());
        return response;
    }
}
