package com.prj.ecommerce.dto.request;

import lombok.Data;

@Data
public class AddCartItemRequest {
    private UpdateProductVariantRequest item;
    private Integer quantity;
}
