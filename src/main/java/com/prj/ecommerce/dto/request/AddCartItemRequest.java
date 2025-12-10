package com.prj.ecommerce.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AddCartItemRequest {
    private UpdateProductVariantRequest item;

    @Min(value = 1)
    private Integer quantity;
}
