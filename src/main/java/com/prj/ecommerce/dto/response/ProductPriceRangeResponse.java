package com.prj.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductPriceRangeResponse {
    private Long productId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
