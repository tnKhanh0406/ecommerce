package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.category.CategoryRequest;
import com.prj.ecommerce.dto.response.category.CategoryResponse;
import com.prj.ecommerce.service.CategoryService;
import com.prj.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping("/")
    public String homePage(Model model) {
        List<CategoryResponse> topLevelCategories = categoryService.getTopLevelCategories();
        model.addAttribute("topLevelCategories", topLevelCategories);
        model.addAttribute("products", productService.getRecommendProducts());
        model.addAttribute("currentCategoryId", null);
        model.addAttribute("currentShopId", null);
        return "user/home";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/categories")
    public String categoriesListPage(@RequestParam(required = false) String search,
                                     @RequestParam(required = false) Long parentId,
                                     Model model) {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        List<CategoryResponse> parentCategories = categories;

        if (parentId != null) {
            if (parentId == -1L) {
                categories = categories.stream()
                        .filter(category -> category.getParentId() == null)
                        .toList();
            } else {
                categories = categories.stream()
                        .filter(category -> parentId.equals(category.getParentId()))
                        .toList();
            }
        }

        if (search != null && !search.trim().isEmpty()) {
            String keyword = search.trim().toLowerCase(Locale.ROOT);
            categories = categories.stream()
                    .filter(category -> category.getName() != null && category.getName().toLowerCase(Locale.ROOT).contains(keyword)
                            || category.getSlug() != null && category.getSlug().toLowerCase(Locale.ROOT).contains(keyword))
                    .toList();
        }

        model.addAttribute("categories", categories);
        model.addAttribute("parentCategories", parentCategories);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("selectedParentId", parentId);

        return "admin/categories";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/categories")
    public String createCategory(@RequestParam String name,
                                 @RequestParam(required = false) String slug,
                                 @RequestParam(required = false) Long parentId,
                                 @RequestParam(required = false) MultipartFile image,
                                 RedirectAttributes redirectAttributes) {
        try {
            CategoryRequest request = new CategoryRequest();
            request.setName(name.trim());
            request.setSlug(buildSlug(slug, name));
            request.setParentId(parentId);
            request.setImage(image);
            categoryService.createCategory(request);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo danh mục thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/categories/{categoryId}/edit")
    public String editCategoryPage(@PathVariable Long categoryId, Model model) {
        CategoryResponse category = categoryService.getCategoryById(categoryId);
        List<CategoryResponse> parentCategories = categoryService.getAllCategories()
                .stream()
                .filter(item -> !item.getId().equals(categoryId))
                .toList();

        model.addAttribute("category", category);
        model.addAttribute("parentCategories", parentCategories);
        return "admin/categoryEdit";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/categories/{categoryId}/edit")
    public String updateCategory(@PathVariable Long categoryId,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String slug,
                                 @RequestParam(required = false) Long parentId,
                                 @RequestParam(required = false) MultipartFile image,
                                 RedirectAttributes redirectAttributes) {
        try {
            CategoryRequest request = new CategoryRequest();
            request.setName(name.trim());
            request.setSlug(buildSlug(slug, name));
            request.setParentId(parentId);
            request.setImage(image);
            categoryService.updateCategory(categoryId, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/categories/" + categoryId + "/edit";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/categories/{categoryId}/delete")
    public String deleteCategory(@PathVariable Long categoryId,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(categoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa danh mục này: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    private String buildSlug(String slugInput, String nameInput) {
        String base = (slugInput != null && !slugInput.trim().isEmpty()) ? slugInput : nameInput;
        String normalized = Normalizer.normalize(base, Normalizer.Form.NFD)                .replaceAll("[^a-z0-9\\s-]", "")

                .replaceAll("\\p{M}", "");

        return normalized
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }
}
