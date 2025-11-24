package com.prj.ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantRequest {
    @NotNull
    private Double price;
    @NotNull
    private Integer stock;

    private String sku;
    private List<ProductVariantAttributeValueRequest> attributeValues;
}
