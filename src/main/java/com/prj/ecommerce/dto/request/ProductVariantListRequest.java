package com.prj.ecommerce.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ProductVariantListRequest {
    private List<UpdateProductVariantRequest> productVariants;
}
