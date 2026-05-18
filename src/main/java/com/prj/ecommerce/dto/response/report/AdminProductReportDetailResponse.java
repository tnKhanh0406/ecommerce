package com.prj.ecommerce.dto.response.report;

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
    private Long productId;
    private String productName;
    private String productDescription;
    private String productImageUrl;
    private String shopName;
    private String shopId;
    private Long totalReportCount;

    private List<ProductReportResponse> reports;
}
