package com.prj.ecommerce.controller;

import com.prj.ecommerce.common.ReferenceType;
import com.prj.ecommerce.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/user/notifications")
    public String notifications(Model model,
                                @RequestParam(required = false) ReferenceType referenceType) {
        model.addAttribute("notifications", notificationService.getAllNotifications(referenceType));
        return "user/notifications";
    }
}
