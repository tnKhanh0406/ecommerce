package com.prj.ecommerce.dto.response.variant;

import com.prj.ecommerce.entity.ProductVariantEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
