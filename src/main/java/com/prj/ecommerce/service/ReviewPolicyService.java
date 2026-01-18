package com.prj.ecommerce.service;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.entity.OrderEntity;
import com.prj.ecommerce.entity.OrderStatusHistoryEntity;
import com.prj.ecommerce.entity.ProductReviewEntity;
import com.prj.ecommerce.repository.OrderStatusHistoryRepository;
import com.prj.ecommerce.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewPolicyService {

    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

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

