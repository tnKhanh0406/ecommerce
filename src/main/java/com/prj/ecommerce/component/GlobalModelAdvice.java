package com.prj.ecommerce.component;

import com.prj.ecommerce.model.UserPrincipal;
import com.prj.ecommerce.service.CartService;
import com.prj.ecommerce.service.NotificationService;
import com.prj.ecommerce.utils.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final NotificationService notificationService;
    private final CartService cartService;

    @ModelAttribute
    public void currentUser(Model model, HttpServletRequest request) {
        // prevent duplicate execution
        if (request.getAttribute("HEADER_LOADED") != null) {
            return;
        }
        request.setAttribute("HEADER_LOADED", true);
        UserPrincipal principal = SecurityUtil.getPrincipal();
        if (principal == null) {
            return;
        }
        model.addAttribute("currentUser", principal);
        model.addAttribute("notifications", notificationService.getTop5Notifications());
        model.addAttribute("headerCartItems", cartService.getTop5CartItems());
        model.addAttribute("cartQuantity", cartService.getCartItemCount());
    }
}