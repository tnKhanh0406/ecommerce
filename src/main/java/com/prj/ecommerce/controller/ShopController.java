package com.prj.ecommerce.controller;

import com.prj.ecommerce.common.UserRole;
import com.prj.ecommerce.dto.request.CreateShopRequest;
import com.prj.ecommerce.dto.request.UpdateShopRequest;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.repository.ShopRepository;
import com.prj.ecommerce.repository.UserRepository;
import com.prj.ecommerce.service.ShopService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @GetMapping("/register")
    public String shopRegisterPage(Model model) {
        try {
            model.addAttribute("createShopRequest", new CreateShopRequest());
            return "shopRegistration";

        } catch (Exception e) {
            return "redirect:/";
        }
    }

    @PostMapping("/register")
    public String registerShop(@ModelAttribute("createShopRequest") @Valid CreateShopRequest request,
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        // Validate form
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult
                    .getAllErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Có lỗi xảy ra");

            model.addAttribute("errorMessage", errorMessage);
            return "shopRegistration";
        }
        try {
            shopService.createShop(request, image);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký shop thành công!");
            return "redirect:/shop/dashboard";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "shopRegistration";
        }
    }

    @GetMapping("/dashboard")
    public String shopDashboard(Model model) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            if (user.getRole() != UserRole.SELLER) {
                return "redirect:/shop/register";
            }

            var shop = shopRepository.findByUser_Id(user.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Shop not found"));

            model.addAttribute("shop", shop);
            return "shopDashboard";

        } catch (Exception e) {
            return "redirect:/";
        }
    }

    @GetMapping("/edit/{shopId}")
    public String editShopPage(Model model, @PathVariable Long shopId) {
        try {
            UpdateShopRequest shopRequest = shopService.getUpdateShopRequest(shopId);
            model.addAttribute("updateShopRequest", shopRequest);
            model.addAttribute("shopId", shopId);
            return "shopEdit";

        } catch (Exception e) {
            return "redirect:/shop/dashboard";
        }
    }

    @PostMapping("/edit/{shopId}")
    public String updateShop(@ModelAttribute("updateShopRequest") @Valid UpdateShopRequest request,
                             @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                             @PathVariable Long shopId,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult
                    .getAllErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Có lỗi xảy ra");

            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("shopId", shopId);
            return "shopEdit";
        }

        try {
            shopService.updateShop(shopId, request, logoFile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật shop thành công!");
            return "redirect:/shop/dashboard";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            model.addAttribute("shopId", shopId);
            return "shopEdit";
        }
    }
}