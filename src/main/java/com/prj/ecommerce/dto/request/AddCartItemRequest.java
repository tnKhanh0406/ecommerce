package com.prj.ecommerce.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AddCartItemRequest {
    private Long variantId;

    @Min(value = 1)
    private Integer quantity;
}
