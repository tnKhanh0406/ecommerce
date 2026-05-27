package com.prj.ecommerce.service;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.entity.OrderEntity;
import com.prj.ecommerce.entity.OrderStatusHistoryEntity;
import com.prj.ecommerce.entity.ProductReviewEntity;
import com.prj.ecommerce.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewPolicyService {

    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public boolean canReview(List<OrderStatusHistoryEntity> histories, int days) {
        Optional<OrderStatusHistoryEntity> completedHistory = histories.stream()
                        .filter(h -> h.getToStatus() == OrderStatus.COMPLETED)
                        .findFirst();

        if (completedHistory.isEmpty()) {
            return false;
        }

        return Duration.between(
                completedHistory.get().getCreatedAt(),
                LocalDateTime.now()
        ).toDays() <= days;
    }

    public boolean canReview(OrderEntity order, int days) {
        OrderStatusHistoryEntity history = orderStatusHistoryRepository
                        .findByOrder_IdAndToStatus(order.getId(), OrderStatus.COMPLETED);

        if (history == null) return false;

        return Duration.between(
                history.getCreatedAt(),
                LocalDateTime.now()
        ).toDays() <= days;
    }

    public boolean canUpdate(ProductReviewEntity review, Integer day) {
        return Duration.between(review.getCreatedAt(), LocalDateTime.now()).toDays() <= day;
    }
}

