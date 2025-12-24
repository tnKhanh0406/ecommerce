package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.response.CategoryResponse;
import com.prj.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/")
    public String homePage(Model model) {
        List<CategoryResponse> topLevelCategories = categoryService.getTopLevelCategories();
        model.addAttribute("topLevelCategories", topLevelCategories);
        return "home";
    }
}
