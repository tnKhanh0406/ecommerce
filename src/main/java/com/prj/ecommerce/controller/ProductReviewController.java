package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.ProductReviewRequest;
import com.prj.ecommerce.dto.request.UpdateReviewRequest;
import com.prj.ecommerce.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductReviewController {
    private final ProductReviewService productReviewService;

    @PostMapping("/user/review/create")
    public String createReview(@Valid @ModelAttribute ProductReviewRequest request,
                               BindingResult result,
                               @RequestParam(value = "images", required = false) List<MultipartFile> images,
                               @RequestParam Long orderId,
                               RedirectAttributes redirectAttributes) {

        // Validation
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin đánh giá");
            return "redirect:/user/order?status=COMPLETED";
        }

        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn đánh giá từ 1-5 sao");
            return "redirect:/user/order?status=COMPLETED";
        }

        try {
            productReviewService.createReview(request, images);
            redirectAttributes.addFlashAttribute("success", "Đánh giá sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/user/order/orderDetail/" + orderId;
    }

    @PostMapping("/user/review/update")
    public String updateReview(@Valid @ModelAttribute UpdateReviewRequest request,
                               BindingResult result,
                               @RequestParam(value = "images", required = false) List<MultipartFile> images,
                               @RequestParam Long orderId,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin đánh giá");
            return "redirect:/user/order/orderDetail/" + orderId;
        }

        if (request.getRating() != null && (request.getRating() < 1 || request.getRating() > 5)) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn đánh giá từ 1-5 sao");
            return "redirect:/user/order/orderDetail/" + orderId;
        }

        try {
            productReviewService.updateReview(request, images);
            redirectAttributes.addFlashAttribute("success", "Cập nhật đánh giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/user/order/orderDetail/" + orderId;
    }
}
