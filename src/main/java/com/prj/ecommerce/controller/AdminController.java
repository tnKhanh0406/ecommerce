package com.prj.ecommerce.controller;

import com.prj.ecommerce.common.Status;
import com.prj.ecommerce.dto.response.AdminProductResponse;
import com.prj.ecommerce.dto.response.CreateShopResponse;
import com.prj.ecommerce.dto.response.ProductDetailResponse;
import com.prj.ecommerce.dto.response.UserResponse;
import com.prj.ecommerce.service.ProductService;
import com.prj.ecommerce.service.ShopService;
import com.prj.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ShopService shopService;
    private final ProductService productService;

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users")
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

    @GetMapping("/users/{userId}")
    public String userDetailPage(@PathVariable Long userId, Model model) {
        UserResponse user = userService.getUserById(userId);
        model.addAttribute("user", user);
        model.addAttribute("statuses", Status.values());
        return "admin/userDetail";
    }

    @PostMapping("/users/{userId}/status")
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

    // ==================== SELLER MANAGEMENT ====================

    @GetMapping("/sellers")
    public String sellersListPage(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String search,
                                  @RequestParam(required = false) Status status,
                                  Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CreateShopResponse> shops = shopService.getAllShops(search, status, pageable);
        
        model.addAttribute("shops", shops.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", shops.getTotalPages());
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("selectedStatus", status != null ? status.toString() : "");
        model.addAttribute("pageSize", size);
        model.addAttribute("statuses", Status.values());
        
        return "admin/sellers";
    }

    @GetMapping("/sellers/{shopId}")
    public String sellerDetailPage(@PathVariable Long shopId, Model model) {
        CreateShopResponse shop = shopService.getShopDetailForAdmin(shopId);
        model.addAttribute("shop", shop);
        return "admin/sellerDetail";
    }

    @PostMapping("/sellers/{shopId}/approve")
    public String approveSeller(@PathVariable Long shopId, RedirectAttributes redirectAttributes) {
        try {
            shopService.approveShop(shopId);
            redirectAttributes.addFlashAttribute("successMessage", "Phê duyệt shop thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/sellers/" + shopId;
    }

    @PostMapping("/sellers/{shopId}/reject")
    public String rejectSeller(@PathVariable Long shopId, RedirectAttributes redirectAttributes) {
        try {
            shopService.rejectShop(shopId);
            redirectAttributes.addFlashAttribute("successMessage", "Từ chối shop thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/sellers/" + shopId;
    }

    // ==================== PRODUCT MANAGEMENT ====================

    @GetMapping("/products")
    public String productsListPage(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String search,
                                   @RequestParam(required = false) Status status,
                                   Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminProductResponse> products = productService.getProductsForAdmin(search, status, pageable);

        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("selectedStatus", status != null ? status.toString() : "");
        model.addAttribute("pageSize", size);
        model.addAttribute("statuses", Status.values());

        return "admin/products";
    }

    @GetMapping("/products/{productId}")
    public String productDetailPage(@PathVariable Long productId, Model model) {
        ProductDetailResponse product = productService.getProductDetailForAdmin(productId);
        model.addAttribute("product", product);
        model.addAttribute("statuses", Status.values());
        return "admin/productDetail";
    }

    @PostMapping("/products/{productId}/status")
    public String updateProductStatus(@PathVariable Long productId,
                                      @RequestParam Status status,
                                      RedirectAttributes redirectAttributes) {
        try {
            productService.updateProductStatusForAdmin(productId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái sản phẩm thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products/" + productId;
    }
}
