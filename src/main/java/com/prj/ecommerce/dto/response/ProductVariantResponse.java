package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.entity.ProductVariantEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private String sku;
    private BigDecimal price;
    private Integer stock;
    private String productName;
    private List<ProductImageResponse> images = new ArrayList<>();
    private List<ProductVariantAttributeValueResponse> attributes = new ArrayList<>();

    public static ProductVariantResponse fromEntity(ProductVariantEntity entity) {
        ProductVariantResponse response = new ProductVariantResponse();
        response.setId(entity.getId());
        response.setSku(entity.getSku());
        response.setPrice(entity.getPrice());
        response.setStock(entity.getStock());
        response.setProductName(entity.getProduct().getName());

        if (entity.getImages() != null) {
            response.setImages(
                    entity.getImages().stream()
                            .map(ProductImageResponse::fromEntity)
                            .collect(Collectors.toList())
            );
        }

        if (entity.getAttributes() != null) {
            response.setAttributes(
                    entity.getAttributes().stream()
                            .map(ProductVariantAttributeValueResponse::fromEntity)
                            .collect(Collectors.toList())
            );
        }
        return response;
    }
}
