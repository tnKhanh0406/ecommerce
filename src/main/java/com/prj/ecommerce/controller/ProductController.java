package com.prj.ecommerce.controller;

import com.prj.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/products/{productId}")
    public String recommendProducts(Model model, @PathVariable Long productId) {
        model.addAttribute("productDetail", productService.getProductDetail(productId));
        return "productDetails";
    }
}
