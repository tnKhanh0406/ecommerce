package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.dto.request.category.CategoryRequest;
import com.prj.ecommerce.dto.response.category.CategoryResponse;
import com.prj.ecommerce.dto.response.category.CategorySidebarItemResponse;
import com.prj.ecommerce.dto.response.category.CategoryTreeResponse;
import com.prj.ecommerce.entity.CategoryEntity;
import com.prj.ecommerce.repository.CategoryRepository;
import com.prj.ecommerce.service.CategoryService;
import com.prj.ecommerce.service.CloudinaryService;
import com.prj.ecommerce.service.ProductService;
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

    public CategoryTreeResponse getCategoryTreeByRoot(Long rootCategoryId) {
        List<CategoryResponse> categories = categoryRepository.findAllCategoryResponse();
        Map<Long, CategoryTreeResponse> map = new HashMap<>();

        // Tạo nodes
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

        // Build tree và lấy root node
        CategoryTreeResponse rootNode = null;
        for (CategoryResponse category : categories) {
            if (category.getId().equals(rootCategoryId)) {
                rootNode = map.get(category.getId());
                break;
            }
        }

        if (rootNode == null) {
            return null;
        }

        // Build tree structure
        for (CategoryResponse category : categories) {
            CategoryTreeResponse node = map.get(category.getId());

            if (category.getParentId() != null) {
                CategoryTreeResponse parent = map.get(category.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        }

        return rootNode;
    }

    /**
     * Xây dựng sidebar categories cho shop hoặc category
     */
    @Override
    public List<CategorySidebarItemResponse> buildSidebarCategories(
            Long shopId,
            Long categoryId) {
        List<CategorySidebarItemResponse> items = new ArrayList<>();

        if (shopId != null) {
            // Lấy các category được sử dụng trong shop
            Set<Long> usedCategoryIds = new LinkedHashSet<>(categoryRepository.getCategoryIdsByShopId(shopId));
            if (usedCategoryIds.isEmpty()) {
                return items;
            }

            List<CategoryTreeResponse> trees = getCategoriesTree();
            for (CategoryTreeResponse tree : trees) {
                CategoryTreeResponse pruned = pruneCategoryTree(tree, usedCategoryIds);
                if (pruned != null) {
                    flattenSidebarCategory(pruned, 0, items);
                }
            }
            return items;
        }

        if (categoryId != null) {
            // Lấy root category và build tree từ nó
            CategoryTreeResponse rootTree = getCategoryTreeByRoot(categoryId);
            if (rootTree != null) {
                flattenSidebarCategory(rootTree, 0, items);
            }
            return items;
        }

        // Nếu không có filter, hiển thị tất cả root categories
        List<CategoryTreeResponse> trees = getCategoriesTree();
        for (CategoryTreeResponse tree : trees) {
            flattenSidebarCategory(tree, 0, items);
        }

        return items;
    }

    /**
     * Lấy root category bằng cách build tree từ flat data
     */
    @Override
    public CategoryResponse getRootCategoryResponse(Long categoryId) {
        List<CategoryResponse> categories = categoryRepository.findAllCategoryResponse();
        Map<Long, CategoryResponse> map = categories.stream()
                .collect(Collectors.toMap(CategoryResponse::getId, Function.identity()));

        CategoryResponse current = map.get(categoryId);
        if (current == null) {
            return null;
        }

        while (current.getParentId() != null) {
            current = map.get(current.getParentId());
        }

        return current;
    }

    private CategoryTreeResponse pruneCategoryTree(
            CategoryTreeResponse node,
            Set<Long> allowedCategoryIds) {
        if (node == null) {
            return null;
        }

        List<CategoryTreeResponse> prunedChildren = new ArrayList<>();
        if (node.getChildren() != null) {
            for (CategoryTreeResponse child : node.getChildren()) {
                CategoryTreeResponse prunedChild = pruneCategoryTree(child, allowedCategoryIds);
                if (prunedChild != null) {
                    prunedChildren.add(prunedChild);
                }
            }
        }

        boolean keepNode = allowedCategoryIds.contains(node.getId()) || !prunedChildren.isEmpty();
        if (!keepNode) {
            return null;
        }

        CategoryTreeResponse copy = new CategoryTreeResponse();
        copy.setId(node.getId());
        copy.setName(node.getName());
        copy.setSlug(node.getSlug());
        copy.setChildren(prunedChildren);
        return copy;
    }

    private void flattenSidebarCategory(
            CategoryTreeResponse category,
            int level,
            List<CategorySidebarItemResponse> items) {
        items.add(new CategorySidebarItemResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                level
        ));

        if (category.getChildren() == null || category.getChildren().isEmpty()) {
            return;
        }

        for (CategoryTreeResponse child : category.getChildren()) {
            flattenSidebarCategory(child, level + 1, items);
        }
    }
}
