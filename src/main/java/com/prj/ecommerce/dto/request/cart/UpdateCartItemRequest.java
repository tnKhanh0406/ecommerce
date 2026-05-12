package com.prj.ecommerce.dto.request.cart;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCartItemRequest {
    private Long cartItemId;

    @Min(1)
    private Integer quantity;

    private List<Long> attributeValueIds;
}
