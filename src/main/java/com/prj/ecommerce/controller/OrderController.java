package com.prj.ecommerce.controller;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.dto.request.order.CreateOrderRequest;
import com.prj.ecommerce.dto.response.cart.CartItemResponse;
import com.prj.ecommerce.dto.response.user.AddressResponse;
import com.prj.ecommerce.dto.response.voucher.VoucherResponse;
import com.prj.ecommerce.entity.CartItemEntity;
import com.prj.ecommerce.model.UserPrincipal;
import com.prj.ecommerce.repository.CartItemRepository;
import com.prj.ecommerce.service.OrderService;
import com.prj.ecommerce.service.UserAddressService;
import com.prj.ecommerce.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final VoucherService voucherService;
    private final UserAddressService userAddressService;
    private final CartItemRepository cartItemRepository;

    @PostMapping("/checkout")
    public String checkoutPage(@ModelAttribute CreateOrderRequest request, Model model) {
        try {
            List<Long> itemIds = request.getCartItemIds();

            if (itemIds == null || itemIds.isEmpty()) {
                return "redirect:/cart";
            }

            // Get current user
            UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            Long userId = principal.getId();

            // Lấy chi tiết cart items từ IDs
            List<CartItemEntity> cartItems = itemIds.stream()
                    .map(id -> cartItemRepository.findById(id).orElse(null))
                    .filter(item -> item != null && item.getCart().getUser().getId().equals(userId))
                    .collect(Collectors.toList());

            if (cartItems.isEmpty()) {
                return "redirect:/cart";
            }

            // Convert to AddCartItemResponse và group by shop
            List<CartItemResponse> selectedItems = cartItems.stream()
                    .map(CartItemResponse::fromEntity)
                    .collect(Collectors.toList());

            Map<Long, List<CartItemResponse>> itemsByShop = selectedItems.stream()
                    .collect(Collectors.groupingBy(item -> item.getProduct().getShopId()));

            // Get vouchers for each shop
            Map<Long, List<VoucherResponse>> vouchersByShop = itemsByShop.keySet().stream()
                    .collect(Collectors.toMap(
                            shopId -> shopId,
                            shopId -> voucherService.getVoucherByShopId(shopId)
                    ));

            // Get user addresses
            List<AddressResponse> userAddresses = userAddressService.getAllAddresses();

            if (userAddresses.isEmpty()) {
                model.addAttribute("errorMessage", "Vui lòng thêm địa chỉ giao hàng trước khi thanh toán");
                return "redirect:/user/address";
            }

            // Add to model
            model.addAttribute("itemsByShop", itemsByShop);
            model.addAttribute("vouchersByShop", vouchersByShop);
            model.addAttribute("addresses", userAddresses);
            model.addAttribute("cartItems", selectedItems);

            return "user/checkout";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/cart";
        }
    }

    @PostMapping("/confirm-order")
    public String createOrder(@ModelAttribute CreateOrderRequest request, Model model) {
        try {
            orderService.createOrder(request);
            return "redirect:/user/order";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/user/order")
    public String orderPage(Model model,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false) OrderStatus status) {
        model.addAttribute("orders", orderService.getOrders(keyword, status).getOrders());
        return "user/order";
    }

    @GetMapping("/user/order/orderDetail/{orderId}")
    public String orderDetailPage(Model model,
                                  @PathVariable Long orderId) {
        model.addAttribute("orderDetail", orderService.getOrderItems(orderId));
        return "user/orderDetail";
    }

    @PostMapping("/user/order/orderDetail/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn hàng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng: " + e.getMessage());
        }

        return "redirect:/user/order/orderDetail/" + orderId;
    }
}
