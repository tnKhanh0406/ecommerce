package com.prj.ecommerce.controller;

import com.prj.ecommerce.common.DiscountType;
import com.prj.ecommerce.dto.request.CreateVoucherRequest;
import com.prj.ecommerce.dto.response.CreateShopResponse;
import com.prj.ecommerce.service.ShopService;
import com.prj.ecommerce.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shop/{shopId}/vouchers")
public class VoucherController {

    private final VoucherService voucherService;
    private final ShopService shopService;

    @GetMapping
    public String voucherManagementPage(@PathVariable Long shopId,
                                        Model model) {
        try {
            CreateShopResponse shop = ensureCurrentSellerShop(shopId);
            model.addAttribute("shop", shop);
            model.addAttribute("vouchers", voucherService.getVoucherByShopId(shopId));
            model.addAttribute("discountTypes", DiscountType.values());
            return "shopVouchers";
        } catch (Exception e) {
            return "redirect:/shop/dashboard";
        }
    }

    @PostMapping("/create")
    public String createVoucher(@PathVariable Long shopId,
                                @Valid @ModelAttribute CreateVoucherRequest request,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        try {
            ensureCurrentSellerShop(shopId);
            if (result.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", getFirstError(result));
                return "redirect:/shop/" + shopId + "/vouchers";
            }

            voucherService.createVoucher(request);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo voucher thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tạo voucher: " + e.getMessage());
        }

        return "redirect:/shop/" + shopId + "/vouchers";
    }

    @PostMapping("/{voucherId}/update")
    public String updateVoucher(@PathVariable Long shopId,
                                @PathVariable Long voucherId,
                                @Valid @ModelAttribute CreateVoucherRequest request,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        try {
            ensureCurrentSellerShop(shopId);
            if (result.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", getFirstError(result));
                return "redirect:/shop/" + shopId + "/vouchers";
            }

            voucherService.updateVoucher(voucherId, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật voucher thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật voucher: " + e.getMessage());
        }

        return "redirect:/shop/" + shopId + "/vouchers";
    }

    @PostMapping("/{voucherId}/delete")
    public String deleteVoucher(@PathVariable Long shopId,
                                @PathVariable Long voucherId,
                                RedirectAttributes redirectAttributes) {
        try {
            ensureCurrentSellerShop(shopId);
            voucherService.deleteVoucher(voucherId);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa voucher thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa voucher: " + e.getMessage());
        }

        return "redirect:/shop/" + shopId + "/vouchers";
    }

    private CreateShopResponse ensureCurrentSellerShop(Long shopId) {
        CreateShopResponse currentShop = shopService.getCurrentUserShop();
        if (!currentShop.getId().equals(shopId)) {
            throw new AccessDeniedException("You do not have permission to access this shop");
        }
        return currentShop;
    }

    private String getFirstError(BindingResult result) {
        return result.getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Dữ liệu không hợp lệ");
    }
}
