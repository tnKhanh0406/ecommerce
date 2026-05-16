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

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

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
        List<CategoryResponse> categories = categoryRepository.findAllCategoryResponse();
        Map<Long, CategoryTreeResponse> map = new HashMap<>();

        // tạo node
        for (CategoryResponse category : categories) {
            map.put(
                    category.getId(),
                    new CategoryTreeResponse(
                            category.getId(),
                            category.getName(),
                            category.getSlug(),
                            new ArrayList<>()
                    )
            );
        }
        List<CategoryTreeResponse> roots = new ArrayList<>();
        // build tree
        for (CategoryResponse category : categories) {
            CategoryTreeResponse node = map.get(category.getId());

            if (category.getParentId() == null) {
                roots.add(node);
            } else {
                CategoryTreeResponse parent = map.get(category.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        }

        return roots;
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
        List<CategoryEntity> categories = categoryRepository.findAll();
        Map<Long, List<Long>> childrenMap = new HashMap<>();

        for (CategoryEntity category : categories) {
            Long parentId = category.getParent() != null
                    ? category.getParent().getId()
                    : null;
            if (parentId != null) {
                childrenMap
                        .computeIfAbsent(parentId, k -> new ArrayList<>())
                        .add(category.getId());
            }
        }

        List<Long> result = new ArrayList<>();
        dfs(rootCategoryId, childrenMap, result);
        return result;
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

    private void dfs(Long categoryId,
            Map<Long, List<Long>> childrenMap,
            List<Long> result) {
        result.add(categoryId);
        List<Long> children =
                childrenMap.getOrDefault(
                        categoryId,
                        Collections.emptyList()
                );
        for (Long childId : children) {
            dfs(childId, childrenMap, result);
        }
    }
}
