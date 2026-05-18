package com.prj.ecommerce.controller;

import com.prj.ecommerce.common.ReportReason;
import com.prj.ecommerce.dto.response.report.AdminProductReportDetailResponse;
import com.prj.ecommerce.dto.response.report.AdminProductReportListItemResponse;
import com.prj.ecommerce.service.ProductReportService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductReportController {

    private final ProductReportService productReportService;

    @GetMapping
    public String listReportedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            log.info("Admin fetching reported products list, page: {}", page);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<AdminProductReportListItemResponse> reportedProducts = 
                    productReportService.getReportedProductsForAdmin(pageable);

            model.addAttribute("reportedProducts", reportedProducts.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", reportedProducts.getTotalPages());
            model.addAttribute("totalElements", reportedProducts.getTotalElements());
            model.addAttribute("pageSize", size);

            return "admin/reportedProducts";
        } catch (Exception e) {
            log.error("Error fetching reported products", e);
            model.addAttribute("errorMessage", "Lỗi khi tải danh sách báo cáo: " + e.getMessage());
            return "admin/reportedProducts";
        }
    }

    @GetMapping("/{productId}")
    public String viewReportDetail(
            @PathVariable Long productId,
            @RequestParam(required = false) Boolean purchased,
            @RequestParam(required = false) ReportReason reason,
            Model model) {
        try {
            log.info("Admin viewing report detail for product: {}, filters - purchased: {}, reason: {}", 
                    productId, purchased, reason);

            AdminProductReportDetailResponse reportDetail = 
                    productReportService.getProductReportDetail(productId, purchased, reason);

            model.addAttribute("reportDetail", reportDetail);
            model.addAttribute("reasons", ReportReason.values());
            model.addAttribute("purchasedFilter", purchased);
            model.addAttribute("reasonFilter", reason);

            return "admin/productReportDetail";
        } catch (Exception e) {
            log.error("Error fetching report detail", e);
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "admin/productReportDetail";
        }
    }

    @PostMapping("/{productId}/block")
    @ResponseBody
    public ResponseEntity<?> blockProductAndResolveReports(
            @PathVariable Long productId,
            @RequestParam(required = false) String adminNote) {
        try {
            log.info("Admin blocking product: {} with note: {}", productId, adminNote);
            
            productReportService.blockProductAndResolveReports(productId, adminNote);
            
            return ResponseEntity.ok(new ApiResponse(true, "Sản phẩm đã bị khóa và tất cả báo cáo đã được xử lý."));
        } catch (Exception e) {
            log.error("Error blocking product", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping("/{productId}/reject")
    @ResponseBody
    public ResponseEntity<?> rejectReportsForProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) String adminNote) {
        try {
            log.info("Admin rejecting reports for product: {} with note: {}", productId, adminNote);
            
            productReportService.rejectReportsForProduct(productId, adminNote);
            
            return ResponseEntity.ok(new ApiResponse(true, 
                    "Các báo cáo đã bị từ chối. Thông báo đã được gửi đến những người báo cáo."));
        } catch (Exception e) {
            log.error("Error rejecting reports", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi: " + e.getMessage()));
        }
    }

    @Getter
    public static class ApiResponse {
        public boolean success;
        public String message;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

    }
}
