package com.prj.ecommerce.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemSummaryResponse {

    private Long cartItemId;

    private Long variantId;

    private String productName;

    private String imageUrl;

    private BigDecimal price;

    private Integer quantity;

    private Integer stock;

    private Long shopId;

    private String shopName;

    private List<String> attributes = new ArrayList<>();

    public CartItemSummaryResponse(
            Long cartItemId,
            Long variantId,
            String productName,
            String imageUrl,
            BigDecimal price,
            Integer quantity,
            Integer stock,
            Long shopId,
            String shopName
    ) {
        this.cartItemId = cartItemId;
        this.variantId = variantId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.stock = stock;
        this.shopId = shopId;
        this.shopName = shopName;
    }
}
