package com.prj.ecommerce.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateAttributeRequest {
    private List<ProductAttributeRequest> attributes;
    private List<ProductVariantRequest> variants;
}
