package com.prj.ecommerce.component;

import com.prj.ecommerce.dto.response.user.UserResponse;
import com.prj.ecommerce.service.CartService;
import com.prj.ecommerce.service.NotificationService;
import com.prj.ecommerce.service.UserService;
import com.prj.ecommerce.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final NotificationService notificationService;
    private final CartService cartService;
    private final UserService userService;

    @ModelAttribute()
    public void currentUser(Model model) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        UserResponse user = userService.getUserById(currentUserId);
        if (user == null) {
            return;
        }
        model.addAttribute("currentUser", user);
        model.addAttribute("notifications", notificationService.getTop5Notifications());
        model.addAttribute("top5CartItems", cartService.getTop5CartItems());
        model.addAttribute("cartQuantity", cartService.getCartItemCount());
    }
}