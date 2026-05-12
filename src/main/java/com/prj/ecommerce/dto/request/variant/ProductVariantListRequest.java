package com.prj.ecommerce.dto.request.variant;

import lombok.Data;

import java.util.List;

@Data
public class ProductVariantListRequest {
    private List<UpdateProductVariantRequest> productVariants;
}
