package com.prj.ecommerce.dto.request.notification;

import com.prj.ecommerce.common.NotificationType;
import com.prj.ecommerce.common.ReferenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private NotificationType type;

    private Long referenceId;

    @NotNull
    private Long userId;

    @NotNull
    private ReferenceType referenceType;

}
