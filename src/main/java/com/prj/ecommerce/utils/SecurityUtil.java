package com.prj.ecommerce.utils;

import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.model.UserPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static UserPrincipal getPrincipal() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return (UserPrincipal) authentication.getPrincipal();
    }

    public static Long getCurrentUserId() {
        UserPrincipal principal = getPrincipal();
        return principal != null ? principal.getId() : null;
    }

    public static String getCurrentUsername() {
        UserPrincipal principal = getPrincipal();
        return principal != null ? principal.getUsername() : null;
    }
}
