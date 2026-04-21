package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.common.NotificationType;
import com.prj.ecommerce.common.ReferenceType;
import com.prj.ecommerce.entity.NotificationEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private Long referenceId;
    private ReferenceType referenceType;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isRead;
    private Long productId;

    public static NotificationResponse fromEntity(NotificationEntity e) {
        return new NotificationResponse(
                e.getId(),
                e.getType(),
                e.getReferenceId(),
                e.getReferenceType(),
                e.getTitle(),
                e.getContent(),
                e.getCreatedAt(),
                e.getIsRead(),
                null // productId will be populated separately
        );
    }
}
