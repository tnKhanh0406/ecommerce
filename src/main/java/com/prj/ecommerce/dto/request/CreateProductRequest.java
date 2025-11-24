package com.prj.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class CreateProductRequest {
    @NotNull
    private Long shopId;

    @NotBlank
    private String name;

    private String description;
    private List<Long> categoryIds;
    private List<ProductAttributeRequest> attributes;
    private List<ProductImageRequest> images;
    private List<ProductVariantRequest> variants;
}
