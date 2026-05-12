package com.prj.ecommerce.dto.request.voucher;

import com.prj.ecommerce.common.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VoucherRequest {
    @NotBlank
    private String code;

    @NotNull
    private DiscountType discountType;

    @NotNull
    private BigDecimal discountValue;

    @NotNull
    private BigDecimal maxDiscount;

    @NotNull
    private BigDecimal minOrderValue;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    @NotNull
    private Integer usageLimit;
}
