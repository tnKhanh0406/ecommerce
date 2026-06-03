package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.OrderItemEntity;
import com.prj.ecommerce.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findAllByOrder_Id(Long orderId);

    @Query("""
            SELECT DISTINCT o FROM OrderEntity o
            JOIN OrderItemEntity oi ON oi.order = o
            WHERE o.user.id = :userId
            AND oi.productId = :productId
            AND (o.orderStatus = 'COMPLETED' OR o.orderStatus = 'DELIVERED')
            """)
    List<OrderEntity> findOrdersByUserAndProductId(
            @Param("userId") Long userId,
            @Param("productId") Long productId);

    @Query("""
        SELECT oi
        FROM OrderItemEntity oi
        JOIN FETCH oi.order
        WHERE oi.order.id IN :orderIds
    """)
    List<OrderItemEntity> findByOrderIds(@Param("orderIds") List<Long> orderIds);
}
