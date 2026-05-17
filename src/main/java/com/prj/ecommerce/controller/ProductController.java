package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.product.ProductFilterRequest;
import com.prj.ecommerce.dto.response.category.CategoryResponse;
import com.prj.ecommerce.dto.response.category.CategorySidebarItemResponse;
import com.prj.ecommerce.dto.response.category.CategoryTreeResponse;
import com.prj.ecommerce.dto.response.product.CreateProductResponse;
import com.prj.ecommerce.dto.response.shop.ShopResponse;
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

        // Delegate sidebar building to service
        List<CategorySidebarItemResponse> sidebarCategories =
                categoryService.buildSidebarCategories(request.getShopId(), request.getCategoryId());

        // Resolve sidebar title
        String sidebarTitle = resolveSidebarTitle(request);

        populateProductListModel(
                model,
                request,
                responses,
                sidebarCategories,
                sidebarTitle
        );

        if (request.getShopId() != null) {
            ShopResponse shop = shopService.getShopById(request.getShopId());
            model.addAttribute("shop", shop);
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

        // Delegate sidebar building to service
        List<CategorySidebarItemResponse> sidebarCategories =
                categoryService.buildSidebarCategories(null, category.getId());

        populateProductListModel(
                model,
                request,
                responses,
                sidebarCategories,
                category.getName()
        );

        return "products";
    }

    private void populateProductListModel(Model model,
                                          ProductFilterRequest request,
                                          Page<CreateProductResponse> responses,
                                          List<CategorySidebarItemResponse> sidebarCategories,
                                          String sidebarTitle) {
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
        model.addAttribute("showCategorySidebar", !sidebarCategories.isEmpty());
        model.addAttribute("sidebarCategories", sidebarCategories);
        model.addAttribute("sidebarTitle", sidebarTitle);
    }

    private String resolveSidebarTitle(ProductFilterRequest request) {
        if (request.getShopId() != null) {
            return "Danh mục của shop";
        }

        if (request.getCategoryId() != null) {
            CategoryResponse rootCategory = categoryService.getRootCategoryResponse(request.getCategoryId());
            return rootCategory != null ? rootCategory.getName() : "Danh mục";
        }

        return "Danh mục";
    }
}