package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.category.CategoryRequest;
import com.prj.ecommerce.dto.response.category.CategoryResponse;
import com.prj.ecommerce.dto.response.category.CategorySidebarItemResponse;
import com.prj.ecommerce.dto.response.category.CategoryTreeResponse;
import com.prj.ecommerce.entity.CategoryEntity;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getTopLevelCategories();
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(Long categoryId);
    List<CategoryTreeResponse> getCategoriesTree();
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long categoryId, CategoryRequest request);
    void deleteCategory(Long categoryId);
    List<Long> getAllCategoryIds(Long rootCategoryId);
    CategoryEntity findBySlug(String slug);
//    CategoryTreeResponse getCategoryTreeByRoot(Long rootCategoryId);
    List<CategorySidebarItemResponse> buildSidebarCategories(Long shopId, Long categoryId);
    CategoryResponse getRootCategoryResponse(Long categoryId);

}
