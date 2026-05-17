package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.report.ProductReportRequest;
import com.prj.ecommerce.service.ProductReportService;
import com.prj.ecommerce.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ProductReportController {

    private final ProductReportService productReportService;

    /**
     * User creates a product report
     * POST /api/reports
     * FormData: productId, reasonCode, description, reportImages (MultipartFile array, max 3)
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createReport(
            ProductReportRequest request,
            MultipartHttpServletRequest multipartRequest) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            
            log.info("User {} creating report for product {}", userId, request.getProductId());
            
            productReportService.createReport(userId, request, multipartRequest);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Báo cáo sản phẩm thành công. Chúng tôi sẽ kiểm tra và xử lý sớm."));
        } catch (Exception e) {
            log.error("Error creating product report", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * Check if user already reported a product (to prevent spam)
     * GET /api/reports/check?productId={id}
     */
    @GetMapping("/check")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> checkUserReport(
            @RequestParam Long productId) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            Boolean hasReported = productReportService.hasUserReportedProduct(userId, productId);
            
            return ResponseEntity.ok(new ApiResponse(true, 
                    hasReported ? "Bạn đã báo cáo sản phẩm này rồi" : "Chưa báo cáo"));
        } catch (Exception e) {
            log.error("Error checking user report", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * Simple API response wrapper
     */
    public static class ApiResponse {
        public boolean success;
        public String message;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
