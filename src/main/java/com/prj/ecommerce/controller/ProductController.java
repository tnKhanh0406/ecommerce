package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.product.ProductFilterRequest;
import com.prj.ecommerce.dto.response.category.CategorySidebarItemResponse;
import com.prj.ecommerce.dto.response.category.CategoryTreeResponse;
import com.prj.ecommerce.dto.response.product.CreateProductResponse;
import com.prj.ecommerce.dto.response.shop.CreateShopResponse;
import com.prj.ecommerce.entity.CategoryEntity;
import com.prj.ecommerce.service.CategoryService;
import com.prj.ecommerce.service.ProductService;
import com.prj.ecommerce.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ShopService shopService;
    private final CategoryService categoryService;

    @GetMapping("/products/{productId}")
    public String recommendProducts(Model model, @PathVariable Long productId) {
        model.addAttribute("productDetail", productService.getProductDetail(productId));
        return "productDetails";
    }

    @GetMapping("/products")
    public String filterProducts(Model model,
                                 @ModelAttribute @Valid ProductFilterRequest request) {
        Page<CreateProductResponse> responses = productService.getProducts(request);
        CategoryEntity selectedCategory = request.getCategoryId() != null
            ? categoryService.findById(request.getCategoryId())
            : null;
        CategoryEntity rootCategory = selectedCategory != null
            ? categoryService.findRootCategory(selectedCategory.getId())
            : null;
        populateProductListModel(model, request, responses, selectedCategory, rootCategory);

        if (request.getShopId() != null) {
            CreateShopResponse shop = shopService.getShopById(request.getShopId());
            model.addAttribute("shop", shop);
        }

        if (selectedCategory != null) {
            model.addAttribute("currentCategoryName", selectedCategory.getName());
        }

        return "products";
    }

    @GetMapping("/c/{slug}")
    public String filterProductsBySlug(Model model,
                                       @PathVariable String slug,
                                       @ModelAttribute @Valid ProductFilterRequest request) {
        CategoryEntity category = categoryService.findBySlug(slug);
        request.setCategoryId(category.getId());

        Page<CreateProductResponse> responses = productService.getProducts(request);
        CategoryEntity selectedCategory = categoryService.findById(category.getId());
        CategoryEntity rootCategory = categoryService.findRootCategory(selectedCategory.getId());
        populateProductListModel(model, request, responses, selectedCategory, rootCategory);
        model.addAttribute("currentCategoryName", category.getName());

        return "products";
    }

    private void populateProductListModel(Model model,
                                          ProductFilterRequest request,
                                          Page<CreateProductResponse> responses,
                                          CategoryEntity selectedCategory,
                                          CategoryEntity rootCategory) {
        boolean showCategorySidebar = request.getCategoryId() != null || request.getShopId() != null;
        List<CategorySidebarItemResponse> sidebarCategories = buildSidebarCategories(request, rootCategory, showCategorySidebar);

        model.addAttribute("products", responses);
        model.addAttribute("keyword", request.getKeyword());
        model.addAttribute("currentCategoryId", request.getCategoryId());
        model.addAttribute("currentShopId", request.getShopId());
        model.addAttribute("currentSortType", request.getSortType());
        model.addAttribute("minPrice", request.getMinPrice());
        model.addAttribute("maxPrice", request.getMaxPrice());
        model.addAttribute("currentPage", responses.getNumber());
        model.addAttribute("totalPages", responses.getTotalPages());
        model.addAttribute("pageSize", request.getSize());
        model.addAttribute("showCategorySidebar", showCategorySidebar && !sidebarCategories.isEmpty());
        model.addAttribute("sidebarCategories", sidebarCategories);
        model.addAttribute("sidebarTitle", resolveSidebarTitle(request, selectedCategory, rootCategory));
    }

    private String resolveSidebarTitle(ProductFilterRequest request,
                                       CategoryEntity selectedCategory,
                                       CategoryEntity rootCategory) {
        if (request.getShopId() != null) {
            return "Danh mục của shop";
        }
        if (rootCategory != null) {
            return rootCategory.getName();
        }
        return "Danh mục";
    }

    private List<CategorySidebarItemResponse> buildSidebarCategories(ProductFilterRequest request,
                                                                     CategoryEntity rootCategory,
                                                                     boolean showCategorySidebar) {
        List<CategorySidebarItemResponse> items = new ArrayList<>();
        if (!showCategorySidebar) {
            return items;
        }

        if (request.getShopId() != null) {
            Set<Long> usedCategoryIds = new LinkedHashSet<>(productService.getCategoryIdsByShopId(request.getShopId()));
            if (usedCategoryIds.isEmpty()) {
                return items;
            }

            for (CategoryTreeResponse tree : categoryService.getCategoriesTree()) {
                CategoryTreeResponse pruned = pruneCategoryTree(tree, usedCategoryIds);
                if (pruned != null) {
                    flattenSidebarCategory(pruned, 0, items);
                }
            }
            return items;
        }

        if (rootCategory != null) {
            CategoryTreeResponse rootTree = CategoryTreeResponse.fromEntity(rootCategory);
            flattenSidebarCategory(rootTree, 0, items);
            return items;
        }

        for (CategoryTreeResponse tree : categoryService.getCategoriesTree()) {
            flattenSidebarCategory(tree, 0, items);
        }

        return items;
    }

    private CategoryTreeResponse pruneCategoryTree(CategoryTreeResponse node, Set<Long> allowedCategoryIds) {
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

    private void flattenSidebarCategory(CategoryTreeResponse category,
                                        int level,
                                        List<CategorySidebarItemResponse> items) {
        items.add(new CategorySidebarItemResponse(category.getId(), category.getName(), category.getSlug(), level));

        if (category.getChildren() == null || category.getChildren().isEmpty()) {
            return;
        }

        for (CategoryTreeResponse child : category.getChildren()) {
            flattenSidebarCategory(child, level + 1, items);
        }
    }
}