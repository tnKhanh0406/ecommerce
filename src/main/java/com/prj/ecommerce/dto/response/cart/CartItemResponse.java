package com.prj.ecommerce.dto.response.cart;

import com.prj.ecommerce.dto.response.variant.ProductVariantResponse;
import com.prj.ecommerce.entity.CartItemEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private ProductVariantResponse product;
    private Integer quantity;

    public static CartItemResponse fromEntity(CartItemEntity entity) {
        return new CartItemResponse(
                entity.getId(),
                ProductVariantResponse.fromEntity(entity.getProductVariant()),
                entity.getQuantity()
        );
    }
}
