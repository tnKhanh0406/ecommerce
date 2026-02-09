package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.ProductFilterRequest;
import com.prj.ecommerce.dto.response.CreateProductResponse;
import com.prj.ecommerce.dto.response.CreateShopResponse;
import com.prj.ecommerce.entity.CategoryEntity;
import com.prj.ecommerce.entity.ShopEntity;
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
import org.springframework.web.bind.annotation.RequestParam;

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
        if (request.getShopId() != null) {
            CreateShopResponse shop = shopService.getShopById(request.getShopId());
            model.addAttribute("shop", shop);
        }
        model.addAttribute("products", responses);
        model.addAttribute("keyword", request.getKeyword());
        model.addAttribute("currentCategoryId", request.getCategoryId());
        model.addAttribute("currentShopId", request.getShopId());
        return "products";
    }

    @GetMapping("/c/{slug}")
    public String filterProductsBySlug(Model model,
                                       @PathVariable String slug,
                                       @ModelAttribute @Valid ProductFilterRequest request) {
        CategoryEntity category = categoryService.findBySlug(slug);
        request.setCategoryId(category.getId());
        Page<CreateProductResponse> responses = productService.getProducts(request);
        model.addAttribute("products", responses);
        model.addAttribute("currentCategoryId", request.getCategoryId());
        return "products";
    }
}