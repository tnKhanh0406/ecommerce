package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.NotificationRequest;
import com.prj.ecommerce.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getAllNotifications(Boolean isRead);
    void sendNotification(NotificationRequest request);
    void deleteNotification(Long notificationId);
}
