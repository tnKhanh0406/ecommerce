package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.*;
import com.prj.ecommerce.dto.request.notification.NotificationRequest;
import com.prj.ecommerce.dto.request.report.ProductReportRequest;
import com.prj.ecommerce.dto.response.report.AdminProductReportDetailResponse;
import com.prj.ecommerce.dto.response.report.AdminProductReportListItemResponse;
import com.prj.ecommerce.dto.response.report.ProductReportResponse;
import com.prj.ecommerce.entity.*;
import com.prj.ecommerce.exception.BadRequestException;
import com.prj.ecommerce.repository.*;
import com.prj.ecommerce.service.ProductReportService;

import com.prj.ecommerce.utils.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;

import com.prj.ecommerce.service.NotificationService;
import com.prj.ecommerce.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductReportServiceImpl implements ProductReportService {

    private final ProductReportRepository productReportRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductImageRepository productImageRepository;
    private final NotificationService notificationService;
    private final CloudinaryService cloudinaryService;

    private static final int MAX_REPORT_IMAGES = 3;
    private static final String SYSTEM_NOTIFICATION_TITLE = "Thông báo hệ thống";

    @Override
    @Transactional
    public void createReport(Long userId, ProductReportRequest request, MultipartHttpServletRequest multipartRequest) {
        log.info("Creating product report for userId: {}, productId: {}", userId, request.getProductId());

        // Validate user exists
        UserEntity user = userRepository.findById(userId).
                orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Validate product exists
        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (productReportRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new BadRequestException("Bạn đã báo cáo sản phẩm này rồi. Không thể báo cáo lại.");
        }

        // Create report entity
        ProductReportEntity report = new ProductReportEntity();
        report.setUser(user);
        report.setProduct(product);
        report.setReasonCode(request.getReasonCode());
        report.setDescription(request.getDescription());
        report.setStatus(ReportStatus.PENDING);

        // Try to find if user purchased this product
        OrderEntity order = findUserOrderForProduct(userId, request.getProductId());
        if (order != null) {
            report.setOrder(order);
        }

        ProductReportEntity savedReport = productReportRepository.save(report);

        // Handle image uploads (up to 3 images)
        uploadReportImages(multipartRequest, savedReport);
    }

    @Override
    public Page<AdminProductReportListItemResponse> getReportedProductsForAdmin(Pageable pageable) {
        return productReportRepository.findReportedProductsSummary(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProductReportDetailResponse getProductReportDetail(
            Long productId,
            Boolean purchasedFilter,
            ReportReason reasonFilter) {
        log.info("Fetching product report details for productId: {}", productId);

        // Get product info
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // Get all reports for this product
        List<ProductReportEntity> reports = productReportRepository.findAllReportsByProductId(productId);

        // Apply filters
        if (purchasedFilter != null || reasonFilter != null) {
            reports = reports.stream()
                    .filter(r -> purchasedFilter == null || 
                           (purchasedFilter && r.getOrder() != null) || 
                           (!purchasedFilter && r.getOrder() == null))
                    .filter(r -> reasonFilter == null || r.getReasonCode().equals(reasonFilter))
                    .collect(Collectors.toList());
        }

        // Build response
        List<ProductReportResponse> reportResponses = reports.stream()
                .map(this::mapToProductReportResponse)
                .collect(Collectors.toList());

        String thumbnailUrl = product.getThumbnailImg();

        return AdminProductReportDetailResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productDescription(product.getDescription())
                .productImageUrl(thumbnailUrl)
                .shopName(product.getShop().getShopName())
                .shopId(String.valueOf(product.getShop().getId()))
                .totalReportCount((long) reportResponses.size())
                .reports(reportResponses)
                .build();
    }

    @Override
    @Transactional
    public void blockProductAndResolveReports(Long productId, String adminNote) {
        log.info("Blocking product {} and resolving all reports", productId);

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // Block the product
        product.setStatus(Status.BLOCKED);
        productRepository.save(product);
        log.info("Product {} blocked", productId);

        // Get all reports for this product
        List<ProductReportEntity> reports = productReportRepository.findAllReportsByProductIdAndStatus(
                productId, ReportStatus.PENDING);

        // Update reports to RESOLVED
        for (ProductReportEntity report : reports) {
            report.setStatus(ReportStatus.RESOLVED);
            report.setAdminNote(adminNote != null ? adminNote : 
                    "Đã xử lý khiếu nại của bạn. Sản phẩm đã bị khóa do vi phạm nguyên tắc cộng đồng.");
            productReportRepository.save(report);

            // Send notification to reporter
            sendReportResolvedNotification(report, true);
        }

        // Send notification to seller
        sendSellerBlockedNotification(product);

        log.info("Resolved {} reports for product {}", reports.size(), productId);
    }

    @Override
    @Transactional
    public void rejectReportsForProduct(Long productId, String adminNote) {
        log.info("Rejecting all reports for product {}", productId);

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // Get all pending reports for this product
        List<ProductReportEntity> reports = productReportRepository.findAllReportsByProductIdAndStatus(
                productId, ReportStatus.PENDING);

        // Update reports to REJECTED
        for (ProductReportEntity report : reports) {
            report.setStatus(ReportStatus.REJECTED);
            report.setAdminNote(adminNote != null ? adminNote : 
                    "Đã xử lý khiếu nại của bạn. Tuy nhiên chúng tôi chưa phát hiện được sai phạm.");
            productReportRepository.save(report);

            // Send notification to reporter
            sendReportResolvedNotification(report, false);
        }

        log.info("Rejected {} reports for product {}", reports.size(), productId);
    }

    @Override
    public Boolean hasUserReportedProduct(Long productId) {
        return productReportRepository.existsByUserIdAndProductId(SecurityUtil.getCurrentUserId(), productId);
    }

    private void uploadReportImages(MultipartHttpServletRequest multipartRequest, ProductReportEntity report) {
        try {
            List<MultipartFile> reportImages = multipartRequest.getFiles("reportImages");

            if (reportImages == null || reportImages.isEmpty()) {
                return;
            }

            int uploadCount = 0;
            for (MultipartFile file : reportImages) {
                if (file.isEmpty()) continue;
                if (uploadCount >= MAX_REPORT_IMAGES) {
                    break;
                }

                // Upload to Cloudinary
                String imageUrl = cloudinaryService.uploadImage(file);

                ProductImageEntity image = new ProductImageEntity();
                image.setImageUrl(imageUrl);
                image.setImageType(ImageType.REPORT);
                image.setReport(report);
                productImageRepository.save(image);

                uploadCount++;
            }
        } catch (Exception e) {
            log.error("Error uploading report images", e);
            throw new RuntimeException("Failed to upload report images: " + e.getMessage());
        }
    }

    private OrderEntity findUserOrderForProduct(Long userId, Long productId) {
        List<OrderEntity> orders = orderItemRepository.findOrdersByUserAndProductId(userId, productId);
        if (!orders.isEmpty()) {
            return orders.get(0);
        }
        return null;
    }

    private ProductReportResponse mapToProductReportResponse(ProductReportEntity report) {
        List<String> imageUrls = report.getReportImages().stream()
                .map(ProductImageEntity::getImageUrl)
                .collect(Collectors.toList());

        Boolean hasPurchased = report.getOrder() != null;

        return ProductReportResponse.builder()
                .id(report.getId())
                .userId(report.getUser().getId())
                .userName(report.getUser().getFullName())
                .userPhone(report.getUser().getPhoneNumber())
                .reasonCode(report.getReasonCode())
                .description(report.getDescription())
                .status(report.getStatus())
                .adminNote(report.getAdminNote())
                .reportImageUrls(imageUrls)
                .userHasPurchased(hasPurchased)
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }

    private void sendReportResolvedNotification(ProductReportEntity report, boolean isBlocked) {
        String content = isBlocked ?
                "Khiếu nại của bạn về sản phẩm \"" + report.getProduct().getName() + 
                "\" đã được xác nhận. Sản phẩm này đã bị khóa do vi phạm nguyên tắc cộng đồng." :
                "Khiếu nại của bạn về sản phẩm \"" + report.getProduct().getName() + 
                "\" đã được kiểm tra. Tuy nhiên, chúng tôi chưa phát hiện được vi phạm. Cảm ơn bạn đã phản hồi!";

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setUserId(report.getUser().getId());
        notificationRequest.setTitle(SYSTEM_NOTIFICATION_TITLE);
        notificationRequest.setContent(content);
        notificationRequest.setType(NotificationType.SYSTEM);
        notificationRequest.setReferenceType(ReferenceType.PRODUCT_REPORT);
        notificationRequest.setReferenceId(report.getId());

        notificationService.sendNotification(notificationRequest);
    }

    private void sendSellerBlockedNotification(ProductEntity product) {
        String content = "Sản phẩm \"" + product.getName() +
                "\" của bạn đã bị khóa do nhân viên hỗ trợ xác nhận vi phạm nguyên tắc cộng đồng. " +
                "Vui lòng liên hệ với bộ phận hỗ trợ khách hàng để biết chi tiết.";

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setUserId(product.getShop().getUser().getId());
        notificationRequest.setTitle(SYSTEM_NOTIFICATION_TITLE);
        notificationRequest.setContent(content);
        notificationRequest.setType(NotificationType.SYSTEM);

        notificationService.sendNotification(notificationRequest);
    }
}
