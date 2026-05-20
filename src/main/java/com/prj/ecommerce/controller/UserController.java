package com.prj.ecommerce.controller;

import com.prj.ecommerce.common.Status;
import com.prj.ecommerce.dto.request.user.ChangePasswordRequest;
import com.prj.ecommerce.dto.request.user.RegisterRequest;
import com.prj.ecommerce.dto.request.user.UpdateProfileRequest;
import com.prj.ecommerce.dto.response.user.UserResponse;
import com.prj.ecommerce.exception.BadRequestException;
import com.prj.ecommerce.exception.UpdateResourceExistException;
import com.prj.ecommerce.service.UserService;
import com.prj.ecommerce.utils.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "user/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "user/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("registerRequest") @Valid RegisterRequest registerRequest,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult
                    .getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            model.addAttribute("error", errorMessage);
            return "user/register";
        }
        userService.registerUser(registerRequest);
        return "redirect:/login";
    }

    @GetMapping("/user/account/profile")
    public String userProfile(Model model) {
        Long userId = SecurityUtil.getCurrentUserId();
        UserResponse user = userService.getUserById(userId);

        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("profile", user);
        return "user/userProfile";
    }

    @PostMapping("/user/account/profile/update")
    public String updateProfile(@Valid @ModelAttribute("profile") UpdateProfileRequest profile,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "user/userProfile";
        }
        try {
            userService.updateUser(profile);
        } catch (UpdateResourceExistException ex) {

            if (ex.getMessage().contains("Email")) {
                bindingResult.rejectValue("email", "duplicate", ex.getMessage());
            } else if (ex.getMessage().contains("Phone")) {
                bindingResult.rejectValue("phoneNumber", "duplicate", ex.getMessage());
            }

            return "user/userProfile";
        }
        redirectAttributes.addFlashAttribute("success", "Cập nhật thành công");
        return "redirect:/user/account/profile";
    }

    @GetMapping("/user/account/password")
    public String changePasswordPage(Model model) {
        if (!model.containsAttribute("password")) {
            model.addAttribute("password", new ChangePasswordRequest());
        }
        return "user/changePassword";
    }


    @PostMapping("/user/account/password/change-password")
    public String changePassword(@Valid @ModelAttribute("password") ChangePasswordRequest password,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "user/changePassword";
        }

        try {
            userService.changePassword(password);
        } catch (BadRequestException ex) {

            if (ex.getMessage().contains("Old")) {
                bindingResult.rejectValue("password", "incorrect", ex.getMessage());
            } else if (ex.getMessage().contains("New")) {
                bindingResult.rejectValue("newPassword", "invalid", ex.getMessage());
            }

            return "user/changePassword";
        }
        redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công");
        return "redirect:/user/account/password";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public String usersListPage(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String search,
                                Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.getAllUsers(search, pageable);

        model.addAttribute("users", users.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("pageSize", size);

        return "admin/users";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/{userId}")
    public String userDetailPage(@PathVariable Long userId, Model model) {
        UserResponse user = userService.getUserById(userId);
        model.addAttribute("user", user);
        model.addAttribute("statuses", Status.values());
        return "admin/userDetail";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users/{userId}/status")
    public String updateUserStatus(@PathVariable Long userId,
                                   @RequestParam Status status,
                                   RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserStatus(userId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái người dùng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users/" + userId;
    }

}
