package com.prj.ecommerce.repository;

import com.prj.ecommerce.dto.response.report.AdminProductReportListItemResponse;
import com.prj.ecommerce.entity.ProductReportEntity;
import com.prj.ecommerce.common.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductReportRepository extends JpaRepository<ProductReportEntity, Long> {

    /**
     * Check if user already reported this product (to prevent spam)
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    /**
     * Check if user has pending report for this product
     */
    Optional<ProductReportEntity> findByUserIdAndProductIdAndStatus(
            Long userId, Long productId, ReportStatus status);

    /**
     * Get paginated list of products with reports, grouped by product
     * Sorted by report count (DESC) then latest report time (DESC)
     */
    @Query("""
            SELECT DISTINCT p.id, p.name, p.images
            FROM ProductReportEntity pr
            JOIN pr.product p
            WHERE pr.status = 'PENDING'
            GROUP BY p.id, p.name
            ORDER BY COUNT(pr.id) DESC, MAX(pr.createdAt) DESC
            """)
    Page<Object[]> findReportedProductsGrouped(Pageable pageable);

    /**
     * Get all reports for a specific product with user info (avoid N+1)
     */
    @Query("""
            SELECT pr FROM ProductReportEntity pr
            LEFT JOIN FETCH pr.user
            LEFT JOIN FETCH pr.reportImages
            WHERE pr.product.id = :productId AND pr.status = 'PENDING'
            ORDER BY pr.createdAt DESC
            """)
    java.util.List<ProductReportEntity> findAllReportsByProductId(@Param("productId") Long productId);

    /**
     * Get total count of pending reports for a product
     */
    @Query("""
            SELECT COUNT(pr) FROM ProductReportEntity pr
            WHERE pr.product.id = :productId AND pr.status = 'PENDING'
            """)
    Long countPendingReportsByProductId(@Param("productId") Long productId);

    /**
     * Get all pending reports for a product to update status (avoid N+1)
     */
    @Query("""
            SELECT pr FROM ProductReportEntity pr
            WHERE pr.product.id = :productId AND pr.status = :status
            """)
    java.util.List<ProductReportEntity> findAllReportsByProductIdAndStatus(
            @Param("productId") Long productId,
            @Param("status") ReportStatus status);

    /**
     * Check if user purchased any variant of this product
     */
    @Query("""
            SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END
            FROM OrderItemEntity oi
            JOIN oi.order o
            WHERE o.user.id = :userId
            AND oi.productId = :productId
            AND (o.orderStatus = 'COMPLETED' OR o.orderStatus = 'DELIVERED')
            """)
    Boolean userHasPurchasedProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * Get summary data for admin dashboard (products with most reports)
     */
    @Query("""
            SELECT new com.prj.ecommerce.dto.response.report.AdminProductReportListItemResponse(
                p.id,
                p.name,
                COALESCE(MIN(pi.imageUrl), ''),
                COUNT(pr),
                MAX(pr.createdAt)
           )
                FROM ProductReportEntity pr
                JOIN pr.product p
                LEFT JOIN ProductImageEntity pi 
                     ON pi.product.id = p.id AND pi.imageType = 'THUMBNAIL'
                WHERE pr.status = 'PENDING'
                GROUP BY p.id, p.name
                ORDER BY COUNT(pr) DESC, MAX(pr.createdAt) DESC
            """)
    Page<AdminProductReportListItemResponse> findReportedProductsSummary(Pageable pageable);
}
