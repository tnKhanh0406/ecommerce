package com.prj.ecommerce.dto.request.attribute;

import com.prj.ecommerce.dto.request.variant.ProductVariantRequest;
import lombok.Data;

import java.util.List;

@Data
public class UpdateAttributeRequest {
    private List<ProductAttributeRequest> attributes;
    private List<ProductVariantRequest> variants;
}
