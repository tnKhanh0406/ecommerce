package com.prj.ecommerce.repository;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.dto.response.order.OrderForShopFlatResponse;
import com.prj.ecommerce.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {
    @Query("""
        SELECT DISTINCT o FROM OrderEntity o
        LEFT JOIN o.orderItems oi
        LEFT JOIN oi.productVariant pv
        LEFT JOIN pv.product p 
        LEFT JOIN p.shop s
        WHERE o.user.id = :userId 
            AND (
                CAST(o.id AS string) LIKE %:keyword%
                OR s.shopName LIKE %:keyword%
                OR p.name LIKE %:keyword%              
            )
        ORDER BY o.createdAt DESC        
    """)
    List<OrderEntity> searchOrders(@Param("keyword") String keyword,
                                    @Param("userId") Long userId);

    List<OrderEntity> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
    List<OrderEntity> findAllByUser_IdAndOrderStatusOrderByCreatedAtDesc(Long userId, OrderStatus orderStatus);
    List<OrderEntity> findAllByShopIdOrderByCreatedAtDesc(Long shopId);
    List<OrderEntity> findAllByShopIdAndOrderStatusOrderByCreatedAtDesc(Long shopId, OrderStatus orderStatus);
    List<OrderEntity> findAllByShopIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long shopId,
                                                                              LocalDateTime start,
                                                                              LocalDateTime end);

    @Query("""
    SELECT new com.prj.ecommerce.dto.response.order.OrderForShopFlatResponse(
        o.id,
        o.createdAt,
        o.note,
        o.orderStatus,
        o.paymentStatus,
        o.receiverName,
        o.total,

        oi.id,
        oi.productId,
        oi.imageUrl,
        oi.price,
        oi.quantity,
        oi.productName,
        oi.productVariantName,
        oi.totalPrice
    )
    FROM OrderEntity o
    JOIN o.orderItems oi
    WHERE o.shopId = :shopId
    AND (:status IS NULL OR o.orderStatus = :status)
    ORDER BY o.createdAt DESC
""")
    List<OrderForShopFlatResponse> findOrdersForShop(
            Long shopId,
            OrderStatus status
    );
}
