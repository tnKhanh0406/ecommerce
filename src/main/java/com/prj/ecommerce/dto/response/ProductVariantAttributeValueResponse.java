package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.entity.ProductVariantAttributeValueEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductVariantAttributeValueResponse {
    private Long id;
    private String attribute;
    private String value;
    private String displayName;

    public static ProductVariantAttributeValueResponse fromEntity(ProductVariantAttributeValueEntity entity) {
        return new ProductVariantAttributeValueResponse(
                entity.getId(),
                entity.getAttributeValue().getProductAttribute().getName(),
                entity.getAttributeValue().getValue(),
                entity.getDisplayName()
        );
    }
}
