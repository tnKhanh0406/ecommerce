package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.response.category.CategoryResponse;
import com.prj.ecommerce.service.CategoryService;
import com.prj.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping("/")
    public String homePage(Model model) {
        List<CategoryResponse> topLevelCategories = categoryService.getTopLevelCategories();
        model.addAttribute("topLevelCategories", topLevelCategories);
        model.addAttribute("products", productService.getRecommendProducts());
        model.addAttribute("currentCategoryId", null);
        model.addAttribute("currentShopId", null);
        return "user/home";
    }
}
