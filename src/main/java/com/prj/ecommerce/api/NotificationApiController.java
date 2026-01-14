package com.prj.ecommerce.api;

import com.prj.ecommerce.common.ReferenceType;
import com.prj.ecommerce.dto.response.NotificationResponse;
import com.prj.ecommerce.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationApiController {
    private final NotificationService notificationService;

    @GetMapping("/{referenceType}")
    public List<NotificationResponse> getAllNotifications(@PathVariable ReferenceType referenceType) {
        return notificationService.getAllNotifications(referenceType);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
