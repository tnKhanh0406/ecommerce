package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.ProductReportRequest;
import com.prj.ecommerce.dto.response.AdminProductReportDetailResponse;
import com.prj.ecommerce.dto.response.AdminProductReportListItemResponse;
import com.prj.ecommerce.common.ReportReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartHttpServletRequest;


public interface ProductReportService {

    /**
     * Create a product report (user action)
     */
    void createReport(Long userId, ProductReportRequest request, MultipartHttpServletRequest multipartRequest);

    /**
     * Get admin dashboard - list of reported products
     */
    Page<AdminProductReportListItemResponse> getReportedProductsForAdmin(Pageable pageable);

    /**
     * Get detailed report information for a specific product
     */
    AdminProductReportDetailResponse getProductReportDetail(Long productId);

    /**
     * Get detailed report information for a specific product with filtering
     */
    AdminProductReportDetailResponse getProductReportDetail(
            Long productId,
            Boolean purchasedFilter,
            ReportReason reasonFilter);

    /**
     * Block product and resolve all reports
     */
    void blockProductAndResolveReports(Long productId, String adminNote);

    /**
     * Reject reports for a product
     */
    void rejectReportsForProduct(Long productId, String adminNote);

    /**
     * Check if user already reported this product (to prevent spam)
     */
    Boolean hasUserReportedProduct(Long userId, Long productId);
}
