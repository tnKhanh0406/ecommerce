package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.dto.request.category.CategoryRequest;
import com.prj.ecommerce.dto.response.category.CategoryResponse;
import com.prj.ecommerce.dto.response.category.CategoryTreeResponse;
import com.prj.ecommerce.entity.CategoryEntity;
import com.prj.ecommerce.repository.CategoryRepository;
import com.prj.ecommerce.service.CategoryService;
import com.prj.ecommerce.service.CloudinaryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public List<CategoryResponse> getTopLevelCategories() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllCategoryResponse();
    }

    @Override
    public CategoryResponse getCategoryById(Long categoryId) {
        return categoryRepository.findCategoryResponseById(categoryId);
    }

    @Override
    public List<CategoryTreeResponse> getCategoriesTree() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(CategoryTreeResponse::fromEntity)
                .toList();
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName(request.getName());
        categoryEntity.setSlug(request.getSlug());
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            categoryEntity.setImageUrl(cloudinaryService.uploadImage(request.getImage()));
        }
        if (request.getParentId() != null) {
            categoryEntity.setParent(categoryRepository.findById(request.getParentId()).orElse(null));
        } else {
            categoryEntity.setParent(null);
        }
        return CategoryResponse.fromEntity(categoryRepository.save(categoryEntity));
    }

    @Override
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        categoryEntity.setName(request.getName());
        categoryEntity.setSlug(request.getSlug());
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            categoryEntity.setImageUrl(cloudinaryService.uploadImage(request.getImage()));
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(categoryId)) {
                throw new IllegalArgumentException("Danh mục cha không hợp lệ");
            }
            categoryEntity.setParent(categoryRepository.findById(request.getParentId()).orElse(null));
        } else {
            categoryEntity.setParent(null);
        }

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
        List<CategoryEntity> allCategories = categoryRepository.findAll();
        Map<Long, CategoryEntity> map = allCategories.stream()
                .collect(Collectors.toMap(CategoryEntity::getId, Function.identity()));
        CategoryEntity category = map.get(categoryId);

        while (category.getParent() != null) {
            category = map.get(category.getParent().getId());
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
