package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.ProductReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReviewEntity, Long> {
    ProductReviewEntity findByOrderItem_Id(Long orderItemId);
    List<ProductReviewEntity> findByProduct_Id(Long productId);
    boolean existsByOrderItem_Id(Long id);
    @Query("""
        select r.orderItem.id
        from ProductReviewEntity r
        where r.orderItem.order.id = :orderId
    """)
    List<Long> findReviewedOrderItemIds(@Param("orderId") Long orderId);
}
