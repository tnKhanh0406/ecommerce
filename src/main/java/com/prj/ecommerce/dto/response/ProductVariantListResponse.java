package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.entity.ProductVariantEntity;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class ProductVariantListResponse {
    private List<ProductVariantResponse> productVariants;

    public static ProductVariantListResponse fromEntity(List<ProductVariantEntity> entities) {
        ProductVariantListResponse response = new ProductVariantListResponse();
        response.setProductVariants(
                entities.stream()
                        .map(ProductVariantResponse::fromEntity)
                        .collect(Collectors.toList())
        );
        return response;
    }
}
