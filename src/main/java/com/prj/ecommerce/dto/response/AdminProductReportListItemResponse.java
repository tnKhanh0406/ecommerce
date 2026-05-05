package com.prj.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProductReportListItemResponse {
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Long reportCount;
    private LocalDateTime latestReportTime;
}
