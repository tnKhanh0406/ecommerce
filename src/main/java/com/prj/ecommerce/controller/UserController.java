package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.RegisterRequest;
import com.prj.ecommerce.dto.request.UpdateProfileRequest;
import com.prj.ecommerce.dto.response.UserResponse;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.exception.UpdateResourceExistException;
import com.prj.ecommerce.service.JWTService;
import com.prj.ecommerce.service.UserService;
import com.prj.ecommerce.utils.SecurityUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            HttpServletResponse response,
                            RedirectAttributes redirectAttributes) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(username, password));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtService.generateToken(userDetails);

            Cookie cookie = new Cookie("access_token", jwt);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            return "redirect:/";
        } catch (AuthenticationException e) {
            redirectAttributes.addFlashAttribute("error", "Sai tên đăng nhập hoặc mật khẩu");
            return "redirect:/login";
        }
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
            return "register";
        }
        userService.registerUser(registerRequest);
        return "redirect:/login";
    }

    @GetMapping("/user/account/profile")
    public String userProfile(Model model) {
        UserEntity user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("profile", UserResponse.fromEntity(user));
        return "userProfile";
    }

    @PostMapping("/user/account/profile/update")
    public String updateProfile(@Valid @ModelAttribute("profile") UpdateProfileRequest profile,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "userProfile";
        }
        try {
            userService.updateUser(profile);
        } catch (UpdateResourceExistException ex) {

            if (ex.getMessage().contains("Email")) {
                bindingResult.rejectValue("email", "duplicate", ex.getMessage());
            } else if (ex.getMessage().contains("Phone")) {
                bindingResult.rejectValue("phoneNumber", "duplicate", ex.getMessage());
            }

            return "userProfile";
        }
        redirectAttributes.addFlashAttribute("success", "Cập nhật thành công");
        return "redirect:/user/account/profile";
    }
}
