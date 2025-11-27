package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.CreateCategoryRequest;
import com.prj.ecommerce.dto.response.CategoryResponse;
import com.prj.ecommerce.dto.response.CategoryTreeResponse;
import com.prj.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CreateCategoryRequest category) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.createCategory(category));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long categoryId,
                                                           @RequestBody CreateCategoryRequest category) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryId, category));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
