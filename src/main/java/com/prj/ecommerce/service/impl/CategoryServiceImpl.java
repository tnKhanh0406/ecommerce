package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.dto.request.CreateCategoryRequest;
import com.prj.ecommerce.dto.response.CategoryResponse;
import com.prj.ecommerce.dto.response.CategoryTreeResponse;
import com.prj.ecommerce.entity.CategoryEntity;
import com.prj.ecommerce.repository.CategoryRepository;
import com.prj.ecommerce.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getTopLevelCategories() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    @Override
    public List<CategoryTreeResponse> getCategoriesTree() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(CategoryTreeResponse::fromEntity)
                .toList();
    }

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName(request.getName());
        categoryEntity.setSlug(request.getSlug());
        categoryEntity.setParent(categoryRepository.findById(request.getParentId()).orElse(null));
        return CategoryResponse.fromEntity(categoryRepository.save(categoryEntity));
    }

    @Override
    public CategoryResponse updateCategory(Long categoryId, CreateCategoryRequest request) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        categoryEntity.setName(request.getName());
        categoryEntity.setSlug(request.getSlug());
        categoryEntity.setParent(categoryRepository.findById(request.getParentId()).orElse(null));
        return CategoryResponse.fromEntity(categoryRepository.save(categoryEntity));
    }

    @Override
    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public List<Long> getAllCategoryIds(Long rootCategoryId) {
        List<Long> result = new ArrayList<>();
        dfs(rootCategoryId, result);
        return result;
    }

    @Override
    public CategoryEntity findById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    @Override
    public CategoryEntity findRootCategory(Long categoryId) {
        CategoryEntity category = findById(categoryId);
        while (category.getParent() != null) {
            category = findById(category.getParent().getId());
        }
        return category;
    }

    @Override
    public CategoryEntity findBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    private void dfs(Long categoryId, List<Long> result) {
        result.add(categoryId);

        List<CategoryEntity> children = categoryRepository.findByParentId(categoryId);

        for (CategoryEntity child : children) {
            dfs(child.getId(), result);
        }
    }
}
