package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.response.CategoryResponse;
import com.prj.ecommerce.dto.response.CategoryTreeResponse;
import com.prj.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/top-level")
    public List<CategoryResponse> getTopLevelCategories() {
        return categoryService.getTopLevelCategories();
    }

    @GetMapping("/")
    public List<CategoryTreeResponse> getCategories() {
        return categoryService.getCategoriesTree();
    }
}
