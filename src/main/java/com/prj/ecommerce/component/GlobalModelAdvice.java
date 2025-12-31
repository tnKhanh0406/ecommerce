package com.prj.ecommerce.component;

import com.prj.ecommerce.dto.response.UserResponse;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.utils.SecurityUtil;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("currentUser")
    public UserResponse currentUser() {
        UserEntity user = SecurityUtil.getCurrentUser();
        if (user == null) return null;

        return UserResponse.fromEntity(user);
    }
}