package com.prj.ecommerce.component;

import com.prj.ecommerce.dto.response.UserResponse;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.service.CartService;
import com.prj.ecommerce.service.NotificationService;
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

    @ModelAttribute()
    public void currentUser(Model model) {
        UserEntity user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return;
        }
        model.addAttribute("currentUser", UserResponse.fromEntity(user));
        model.addAttribute("notifications", notificationService.getTop5Notifications());
        model.addAttribute("cartItems", cartService.getTop5CartItems());
        int quantity = 0;
        if (cartService.getCartItems() != null) {
            quantity = cartService.getCartItems().size();
        }
        model.addAttribute("cartQuantity", quantity);
    }
}