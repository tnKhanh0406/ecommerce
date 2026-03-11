package com.prj.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductPriceRangeResponse {
    private Long productId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
