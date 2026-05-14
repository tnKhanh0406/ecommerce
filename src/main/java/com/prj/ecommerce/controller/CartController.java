package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.cart.AddCartItemRequest;
import com.prj.ecommerce.dto.response.cart.CartItemResponse;
import com.prj.ecommerce.dto.response.cart.CartItemSummaryResponse;
import com.prj.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public String viewCart(Model model) {

        List<CartItemSummaryResponse> cartItems =
                cartService.getCartItems();

        Map<Long, List<CartItemSummaryResponse>> itemsByShop =
                cartItems.stream()
                        .collect(Collectors.groupingBy(
                                CartItemSummaryResponse::getShopId
                        ));

        model.addAttribute("itemsByShop", itemsByShop);
        model.addAttribute("cartItems", cartItems);

        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@Valid @ModelAttribute AddCartItemRequest request,
                            RedirectAttributes redirectAttributes) {
        try {
            cartService.addCartItem(request);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng!");
            return "redirect:/cart";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/products/" + request.getVariantId();
        }
    }

    @PostMapping("/delete")
    public String deleteCartItem(@RequestParam Long cartItemId,
                                 RedirectAttributes redirectAttributes) {
        try {
            cartService.deleteCartItem(cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }
}
