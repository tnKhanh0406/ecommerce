package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.common.ReportReason;
import com.prj.ecommerce.common.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReportResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userPhone;
    private ReportReason reasonCode;
    private String description;
    private ReportStatus status;
    private String adminNote;
    private List<String> reportImageUrls;
    private Boolean userHasPurchased;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
