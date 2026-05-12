package com.prj.ecommerce.dto.request.report;

import com.prj.ecommerce.common.ReportReason;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReportRequest {
    private Long productId;
    private ReportReason reasonCode;
    private String description;
}
