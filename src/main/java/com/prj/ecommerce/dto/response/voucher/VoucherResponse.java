package com.prj.ecommerce.dto.response.voucher;

import com.prj.ecommerce.common.DiscountType;
import com.prj.ecommerce.entity.VoucherEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoucherResponse {
    private Long id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderValue;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer usageLimit;
    private Integer usedCount;
    private Long shopId;
    private LocalDateTime createdAt;

    public static VoucherResponse fromEntity(VoucherEntity e) {
        return new VoucherResponse(
                e.getId(),
                e.getCode(),
                e.getDiscountType(),
                e.getDiscountValue(),
                e.getMaxDiscount(),
                e.getMinOrderValue(),
                e.getStartAt(),
                e.getEndAt(),
                e.getUsageLimit(),
                e.getUsedCount(),
                e.getShop().getId(),
                e.getCreatedAt()
        );
    }
}
