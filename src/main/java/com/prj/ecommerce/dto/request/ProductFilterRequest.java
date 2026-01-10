package com.prj.ecommerce.dto.request;

import com.prj.ecommerce.common.ProductSortType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductFilterRequest {
    private String keyword;
    private Long categoryId;
    private Long shopId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private ProductSortType sortType = ProductSortType.NEWEST;
    @Min(0)
    private Integer page = 0;

    @Min(1)
    @Max(100)
    private Integer size = 20;
}
