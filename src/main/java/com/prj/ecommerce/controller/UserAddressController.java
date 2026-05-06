package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.user.CreateAddressRequest;
import com.prj.ecommerce.service.UserAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UserAddressController {
    private final UserAddressService userAddressService;

    @GetMapping("/user/account/address")
    public String userAddress(Model model) {
        model.addAttribute("addresses", userAddressService.getAllAddresses());
        model.addAttribute("addressForm", new CreateAddressRequest());
        return "address";
    }

    @PostMapping("/user/account/address/create")
    public String createAddress(@Valid @ModelAttribute("addressForm") CreateAddressRequest request,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.addressForm", result);
            redirectAttributes.addFlashAttribute("addressForm", request);
            return "redirect:/user/account/address";
        }

        userAddressService.createAddress(request);
        redirectAttributes.addFlashAttribute("success", "Address added successfully");
        return "redirect:/user/account/address";
    }

    @PostMapping("/user/account/address/update")
    public String updateAddress(@ModelAttribute CreateAddressRequest request,
                                @RequestParam Long addressId,
                                RedirectAttributes redirectAttributes) {
        userAddressService.updateAddress(addressId, request);
        redirectAttributes.addFlashAttribute("success", "Cập nhật địa chỉ thành công");
        return "redirect:/user/account/address";
    }

    @PostMapping("/user/account/address/delete/{id}")
    public String deleteAddress(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        userAddressService.deleteAddress(id);
        redirectAttributes.addFlashAttribute("success", "Address deleted");
        return "redirect:/user/account/address";
    }

    @PostMapping("/user/account/address/set-default/{id}")
    public String setDefault(@PathVariable Long id) {
        userAddressService.setDefaultAddress(id);
        return "redirect:/user/account/address";
    }


}
