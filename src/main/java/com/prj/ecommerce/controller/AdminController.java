package com.prj.ecommerce.controller;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.common.Status;
import com.prj.ecommerce.dto.request.category.CategoryRequest;
import com.prj.ecommerce.dto.response.product.AdminProductResponse;
import com.prj.ecommerce.dto.response.category.CategoryResponse;
import com.prj.ecommerce.dto.response.order.OrderResponse;
import com.prj.ecommerce.dto.response.shop.ShopResponse;
import com.prj.ecommerce.dto.response.product.ProductDetailResponse;
import com.prj.ecommerce.dto.response.user.UserResponse;
import com.prj.ecommerce.service.CategoryService;
import com.prj.ecommerce.service.OrderService;
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
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ShopService shopService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;

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
        Page<ShopResponse> shops = shopService.getAllShops(search, status, pageable);
        
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
        ShopResponse shop = shopService.getShopById(shopId);
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

    // ==================== ORDER MANAGEMENT ====================

    @GetMapping("/orders")
    public String ordersListPage(@RequestParam(required = false) String search,
                                 @RequestParam(required = false) OrderStatus status,
                                 Model model) {
        model.addAttribute("orders", orderService.getOrdersForAdmin(search, status).getOrders());
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("currentStatus", status);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders";
    }

    @GetMapping("/orders/{orderId}")
    public String orderDetailPage(@PathVariable Long orderId,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            OrderResponse order = orderService.getOrderDetailForAdmin(orderId);
            model.addAttribute("order", order);
            model.addAttribute("statuses", OrderStatus.values());
            return "admin/orderDetail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng: " + e.getMessage());
            return "redirect:/admin/orders";
        }
    }

    @PostMapping("/orders/{orderId}/status")
    public String updateOrderStatusByAdmin(@PathVariable Long orderId,
                                           @RequestParam("status") OrderStatus status,
                                           RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatusByAdmin(orderId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật đơn hàng: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }

    // ==================== CATEGORY MANAGEMENT ====================

    @GetMapping("/categories")
    public String categoriesListPage(@RequestParam(required = false) String search,
                                     @RequestParam(required = false) Long parentId,
                                     Model model) {
        List<CategoryResponse> categories = categoryService.getAllCategories();

        if (parentId != null) {
            if (parentId == -1L) {
            categories = categories.stream()
                .filter(category -> category.getParentId() == null)
                .toList();
            } else {
            categories = categories.stream()
                    .filter(category -> parentId.equals(category.getParentId()))
                    .toList();
            }
        }

        if (search != null && !search.trim().isEmpty()) {
            String keyword = search.trim().toLowerCase(Locale.ROOT);
            categories = categories.stream()
                    .filter(category -> category.getName() != null && category.getName().toLowerCase(Locale.ROOT).contains(keyword)
                            || category.getSlug() != null && category.getSlug().toLowerCase(Locale.ROOT).contains(keyword))
                    .toList();
        }

        model.addAttribute("categories", categories);
        model.addAttribute("parentCategories", categoryService.getAllCategories());
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("selectedParentId", parentId);

        return "admin/categories";
    }

    @PostMapping("/categories")
    public String createCategory(@RequestParam String name,
                                 @RequestParam(required = false) String slug,
                                 @RequestParam(required = false) Long parentId,
                                 @RequestParam(required = false) MultipartFile image,
                                 RedirectAttributes redirectAttributes) {
        try {
            CategoryRequest request = new CategoryRequest();
            request.setName(name.trim());
            request.setSlug(buildSlug(slug, name));
            request.setParentId(parentId);
            request.setImage(image);
            categoryService.createCategory(request);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo danh mục thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/{categoryId}/edit")
    public String editCategoryPage(@PathVariable Long categoryId, Model model) {
        CategoryResponse category = categoryService.getCategoryById(categoryId);
        List<CategoryResponse> parentCategories = categoryService.getAllCategories()
                .stream()
                .filter(item -> !item.getId().equals(categoryId))
                .toList();

        model.addAttribute("category", category);
        model.addAttribute("parentCategories", parentCategories);
        return "admin/categoryEdit";
    }

    @PostMapping("/categories/{categoryId}/edit")
    public String updateCategory(@PathVariable Long categoryId,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String slug,
                                 @RequestParam(required = false) Long parentId,
                                 @RequestParam(required = false) MultipartFile image,
                                 RedirectAttributes redirectAttributes) {
        try {
            CategoryRequest request = new CategoryRequest();
            request.setName(name.trim());
            request.setSlug(buildSlug(slug, name));
            request.setParentId(parentId);
            request.setImage(image);
            categoryService.updateCategory(categoryId, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/categories/" + categoryId + "/edit";
        }
    }

    @PostMapping("/categories/{categoryId}/delete")
    public String deleteCategory(@PathVariable Long categoryId,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(categoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa danh mục này: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    private String buildSlug(String slugInput, String nameInput) {
        String base = (slugInput != null && !slugInput.trim().isEmpty()) ? slugInput : nameInput;
        String normalized = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return normalized
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }
}
