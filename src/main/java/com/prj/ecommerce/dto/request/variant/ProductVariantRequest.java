package com.prj.ecommerce.dto.request.variant;

import com.prj.ecommerce.dto.request.image.ProductImageRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantRequest {
    private String sku;

    @NotNull
    private BigDecimal price;
    @NotNull
    private Integer stock;

    private List<ProductVariantAttributeValueRequest> attributes;
    private List<ProductImageRequest> images;
}
