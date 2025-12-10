package com.prj.ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateProductVariantRequest {
    @NotNull
    private Long id;

    private String sku;

    @NotNull
    private BigDecimal price;

    @NotNull
    private Integer stock;

    private List<ProductImageRequest> images;
}
