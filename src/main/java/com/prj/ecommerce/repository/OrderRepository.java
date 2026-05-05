package com.prj.ecommerce.repository;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
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
    @Query("""
        SELECT DISTINCT o FROM OrderEntity o
        LEFT JOIN o.user u
        WHERE (
            CAST(o.id AS string) LIKE %:keyword%
            OR LOWER(o.shopName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(o.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY o.createdAt DESC
    """)
    List<OrderEntity> searchOrdersForAdmin(@Param("keyword") String keyword);

    List<OrderEntity> findAllByOrderByCreatedAtDesc();
    List<OrderEntity> findAllByOrderStatusOrderByCreatedAtDesc(OrderStatus orderStatus);

    List<OrderEntity> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
    List<OrderEntity> findAllByUser_IdAndOrderStatusOrderByCreatedAtDesc(Long userId, OrderStatus orderStatus);
    List<OrderEntity> findAllByShopIdOrderByCreatedAtDesc(Long shopId);
    List<OrderEntity> findAllByShopIdAndOrderStatusOrderByCreatedAtDesc(Long shopId, OrderStatus orderStatus);
    List<OrderEntity> findAllByShopIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long shopId,
                                                                              LocalDateTime start,
                                                                              LocalDateTime end);
}
