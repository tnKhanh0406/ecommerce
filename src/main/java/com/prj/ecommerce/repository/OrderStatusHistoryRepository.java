package com.prj.ecommerce.repository;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.entity.OrderStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistoryEntity, Long> {
    OrderStatusHistoryEntity findByOrder_IdAndToStatus(Long orderId, OrderStatus status);
}
