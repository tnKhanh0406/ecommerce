package com.prj.ecommerce.repository;

import com.prj.ecommerce.dto.response.report.AdminProductReportListItemResponse;
import com.prj.ecommerce.entity.ProductReportEntity;
import com.prj.ecommerce.common.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductReportRepository extends JpaRepository<ProductReportEntity, Long> {
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("""
            SELECT pr FROM ProductReportEntity pr
            LEFT JOIN FETCH pr.user
            LEFT JOIN FETCH pr.reportImages
            WHERE pr.product.id = :productId AND pr.status = 'PENDING'
            ORDER BY pr.createdAt DESC
            """)
    List<ProductReportEntity> findAllReportsByProductId(@Param("productId") Long productId);

    @Query("""
            SELECT pr FROM ProductReportEntity pr
            WHERE pr.product.id = :productId AND pr.status = :status
            """)
    List<ProductReportEntity> findAllReportsByProductIdAndStatus(
            @Param("productId") Long productId,
            @Param("status") ReportStatus status);

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
