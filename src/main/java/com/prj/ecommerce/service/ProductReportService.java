package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.report.ProductReportRequest;
import com.prj.ecommerce.dto.response.report.AdminProductReportDetailResponse;
import com.prj.ecommerce.dto.response.report.AdminProductReportListItemResponse;
import com.prj.ecommerce.common.ReportReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartHttpServletRequest;


public interface ProductReportService {
    void createReport(Long userId, ProductReportRequest request, MultipartHttpServletRequest multipartRequest);
    Page<AdminProductReportListItemResponse> getReportedProductsForAdmin(Pageable pageable);
    AdminProductReportDetailResponse getProductReportDetail(
            Long productId,
            Boolean purchasedFilter,
            ReportReason reasonFilter);
    void blockProductAndResolveReports(Long productId, String adminNote);
    void rejectReportsForProduct(Long productId, String adminNote);
    Boolean hasUserReportedProduct(Long productId);
}
