package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.entity.CartItemEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddCartItemResponse {
    private Long id;
    private ProductVariantResponse product;
    private Integer quantity;

    public static AddCartItemResponse fromEntity(CartItemEntity entity) {
        return new AddCartItemResponse(
                entity.getId(),
                ProductVariantResponse.fromEntity(entity.getProductVariant()),
                entity.getQuantity()
        );
    }
}
