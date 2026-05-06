package com.prj.ecommerce.service;

import com.prj.ecommerce.common.ReferenceType;
import com.prj.ecommerce.dto.request.notification.NotificationRequest;
import com.prj.ecommerce.dto.response.notification.NotificationResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getTop5Notifications();
    List<NotificationResponse> getAllNotifications(ReferenceType referenceType);
    void sendNotification(NotificationRequest request);
    void deleteNotification(Long notificationId);
}
