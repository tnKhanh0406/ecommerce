package com.prj.ecommerce.repository;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.entity.OrderStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistoryEntity, Long> {
    OrderStatusHistoryEntity findByOrder_IdAndToStatus(Long orderId, OrderStatus status);
    @Query("""
        SELECT h
        FROM OrderStatusHistoryEntity h
        JOIN FETCH h.order
        WHERE h.order.id IN :orderIds
    """)
    List<OrderStatusHistoryEntity> findByOrderIds(List<Long> orderIds);
}
