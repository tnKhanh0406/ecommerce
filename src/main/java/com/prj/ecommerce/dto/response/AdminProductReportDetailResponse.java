package com.prj.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProductReportDetailResponse {
    // Product info
    private Long productId;
    private String productName;
    private String productDescription;
    private String productImageUrl;
    private String shopName;
    private String shopId;
    private Long totalReportCount;

    // Product variants (simplified)
    private List<VariantInfo> variants;

    // Report details
    private List<ProductReportResponse> reports;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VariantInfo {
        private Long variantId;
        private String variantName;
        private BigDecimal price;
        private Integer stock;
    }
}
