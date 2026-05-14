package com.prj.ecommerce.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HeaderCartItemResponse {
    private Long id;
    private Long variantId;
    private String productName;
    private String imageUrl;
    private BigDecimal price;
    private Integer quantity;
}
