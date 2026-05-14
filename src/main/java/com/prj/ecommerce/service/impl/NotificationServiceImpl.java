package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.ReferenceType;
import com.prj.ecommerce.dto.request.notification.NotificationRequest;
import com.prj.ecommerce.dto.response.notification.NotificationResponse;
import com.prj.ecommerce.entity.NotificationEntity;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.repository.NotificationRepository;
import com.prj.ecommerce.repository.UserRepository;
import com.prj.ecommerce.service.NotificationService;
import com.prj.ecommerce.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        return SecurityUtil.getCurrentUserId();
    }

    @Override
    public List<NotificationResponse> getTop5Notifications() {
        List<NotificationResponse> responses = notificationRepository.findTop5ByUser_IdOrderByCreatedAtDesc(getCurrentUserId())
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
        return responses;
    }

    @Override
    public List<NotificationResponse> getAllNotifications(ReferenceType referenceType) {
        List<NotificationEntity> notificationEntities = notificationRepository.findAllByUser_IdAndReferenceType(getCurrentUserId(), referenceType);
        List<NotificationResponse> responses = notificationEntities.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
        return responses;
    }

//    private List<NotificationResponse> enrichProductIds(List<NotificationResponse> responses) {
//        for (NotificationResponse response : responses) {
//            if (response.getReferenceType() == ReferenceType.REVIEW) {
//                Long productId = productReviewRepository.findProductIdByReviewId(response.getReferenceId());
//                response.setProductId(productId);
//            }
//        }
//        return responses;
//    }

    @Override
    public void sendNotification(NotificationRequest request) {
        NotificationEntity notification = new NotificationEntity();
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        notification.setContent(request.getContent());
        notification.setReferenceId(request.getReferenceId());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setReferenceType(request.getReferenceType());
        notification.setUser(user);

        notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}
