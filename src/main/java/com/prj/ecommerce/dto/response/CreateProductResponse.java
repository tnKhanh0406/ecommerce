package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.entity.ProductEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductResponse {
    private Long id;
    private String name;
    private String description;
    private Integer soldCount;
    private String imageUrl;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    public static CreateProductResponse fromEntity(ProductEntity p) {
        CreateProductResponse r = new CreateProductResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setDescription(p.getDescription());
        r.setSoldCount(p.getSoldCount());
        r.setImageUrl(p.getImages().get(0).getImageUrl());
        return r;
    }
}
