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
        model.addAttribute("orders", orderService.getOrdersForAdmin(search, status));
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
}
