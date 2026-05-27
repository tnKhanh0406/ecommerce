package com.prj.ecommerce.dto.response.image;

import com.prj.ecommerce.entity.ProductImageEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
