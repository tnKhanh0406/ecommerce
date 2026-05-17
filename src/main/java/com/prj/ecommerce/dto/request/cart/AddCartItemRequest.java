package com.prj.ecommerce.dto.request.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCartItemRequest {
    @NotNull(message = "Vui lòng chọn variant")
    private Long variantId;

    @Min(value = 1, message = "Số lượng phải >= 1")
    private Integer quantity;
}
