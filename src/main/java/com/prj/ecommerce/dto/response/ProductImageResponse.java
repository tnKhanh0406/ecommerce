package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.entity.ProductImageEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductImageResponse {
    private Long id;
    private String imageUrl;
    private String imageType;

    public static ProductImageResponse fromEntity(ProductImageEntity productImageEntity) {
        return new ProductImageResponse(
                productImageEntity.getId(),
                productImageEntity.getImageUrl(),
                productImageEntity.getImageType().toString()
        );
    }
}
