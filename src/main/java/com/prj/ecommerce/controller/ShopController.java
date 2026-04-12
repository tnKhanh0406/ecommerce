package com.prj.ecommerce.controller;

import com.prj.ecommerce.common.UserRole;
import com.prj.ecommerce.dto.request.*;
import com.prj.ecommerce.dto.response.CategoryResponse;
import com.prj.ecommerce.dto.response.CategoryTreeResponse;
import com.prj.ecommerce.dto.response.CreateProductResponse;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.repository.ShopRepository;
import com.prj.ecommerce.repository.UserRepository;
import com.prj.ecommerce.service.CategoryService;
import com.prj.ecommerce.service.CloudinaryService;
import com.prj.ecommerce.service.ProductService;
import com.prj.ecommerce.service.ShopService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/register")
    public String shopRegisterPage(Model model) {
        try {
            model.addAttribute("createShopRequest", new CreateShopRequest());
            return "shopRegistration";

        } catch (Exception e) {
            return "redirect:/";
        }
    }

    @PostMapping("/register")
    public String registerShop(@ModelAttribute("createShopRequest") @Valid CreateShopRequest request,
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        // Validate form
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult
                    .getAllErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Có lỗi xảy ra");

            model.addAttribute("errorMessage", errorMessage);
            return "shopRegistration";
        }
        try {
            shopService.createShop(request, image);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký shop thành công!");
            return "redirect:/shop/dashboard";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "shopRegistration";
        }
    }

    @GetMapping("/dashboard")
    public String shopDashboard(Model model) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            if (user.getRole() != UserRole.SELLER) {
                return "redirect:/shop/register";
            }

            var shop = shopRepository.findByUser_Id(user.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Shop not found"));

            model.addAttribute("shop", shop);
            return "shopDashboard";

        } catch (Exception e) {
            return "redirect:/";
        }
    }

    @GetMapping("/edit/{shopId}")
    public String editShopPage(Model model, @PathVariable Long shopId) {
        try {
            UpdateShopRequest shopRequest = shopService.getUpdateShopRequest(shopId);
            model.addAttribute("updateShopRequest", shopRequest);
            model.addAttribute("shopId", shopId);
            return "shopEdit";

        } catch (Exception e) {
            return "redirect:/shop/dashboard";
        }
    }

    @PostMapping("/edit/{shopId}")
    public String updateShop(@ModelAttribute("updateShopRequest") @Valid UpdateShopRequest request,
                             @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                             @PathVariable Long shopId,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult
                    .getAllErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Có lỗi xảy ra");

            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("shopId", shopId);
            return "shopEdit";
        }

        try {
            shopService.updateShop(shopId, request, logoFile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật shop thành công!");
            return "redirect:/shop/dashboard";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            model.addAttribute("shopId", shopId);
            return "shopEdit";
        }
    }

    @GetMapping("/{shopId}/products")
    public String shopProductList(Model model,
                                  @PathVariable Long shopId,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        try {
            Page<CreateProductResponse> products = productService.getProductsByShopId(shopId, page, size);

            model.addAttribute("shop", shopService.getShopById(shopId));
            model.addAttribute("products", products);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", products.getTotalPages());

            return "shopProducts";

        } catch (Exception e) {
            return "redirect:/shop/dashboard";
        }
    }

    @GetMapping("/{shopId}/products/create")
    public String createProductPage(Model model,
                                    @PathVariable Long shopId) {
        try {
            List<CategoryTreeResponse> categories = categoryService.getCategoriesTree();

            model.addAttribute("shop", shopService.getShopById(shopId));
            model.addAttribute("categories", categories);

            return "ShopProductCreate";

        } catch (Exception e) {
            return "redirect:/shop/dashboard";
        }
    }

    @PostMapping("/{shopId}/products/create")
    public String createProduct(@RequestParam String name,
                                @RequestParam String description,
                                @RequestParam List<Long> categoryIds,
                                @RequestParam(required = false) List<MultipartFile> productImages,
                                @RequestParam Map<String, String> attributeParams,
                                @RequestParam Map<String, String> variantParams,
                                @RequestParam(required = false) List<MultipartFile> variantImages,
                                @PathVariable Long shopId,
                                RedirectAttributes redirectAttributes) {
        try {
            // ✅ Parse attributes từ form
            List<ProductAttributeRequest> attributes = parseAttributes(attributeParams);

            // ✅ Parse variants từ form + upload images
            List<ProductVariantRequest> variants = parseVariants(variantParams, variantImages);

            // ✅ Upload product images
            List<ProductImageRequest> productImageRequests = new ArrayList<>();
            if (productImages != null) {
                for (MultipartFile file : productImages) {
                    if (file != null && !file.isEmpty()) {
                        String imageUrl = cloudinaryService.uploadImage(file);
                        productImageRequests.add(new ProductImageRequest(imageUrl));
                    }
                }
            }

            // ✅ Create product request
            CreateProductRequest request = new CreateProductRequest();
            request.setShopId(shopId);
            request.setName(name);
            request.setDescription(description);
            request.setCategoryIds(categoryIds);
            request.setAttributes(attributes);
            request.setVariants(variants);
            request.setImages(productImageRequests);

            productService.createProduct(request);

            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
            return String.format("redirect:/shop/%d/products", shopId);

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return String.format("redirect:/shop/%d/products/create", shopId);
        }
    }

    private List<ProductAttributeRequest> parseAttributes(Map<String, String> params) {
        Map<Integer, ProductAttributeRequest> attrMap = new LinkedHashMap<>();

        for (String key : params.keySet()) {
            if (key.startsWith("attr_name_")) {
                String indexStr = key.substring("attr_name_".length());
                try {
                    int index = Integer.parseInt(indexStr);
                    String attrName = params.get(key);
                    String valueStr = params.get("attr_values_" + index);

                    if (attrName != null && !attrName.isBlank() && valueStr != null && !valueStr.isBlank()) {
                        List<ProductAttributeValueRequest> values = new ArrayList<>();

                        // Split by newline or comma
                        String[] valueArray = valueStr.split("[,\n]");
                        for (String val : valueArray) {
                            String trimmed = val.trim();
                            if (!trimmed.isBlank()) {
                                values.add(new ProductAttributeValueRequest(trimmed));
                            }
                        }

                        if (!values.isEmpty()) {
                            attrMap.put(index, new ProductAttributeRequest(attrName, values));
                        }
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid index
                }
            }
        }

        return new ArrayList<>(attrMap.values());
    }

    private List<ProductVariantRequest> parseVariants(Map<String, String> params,
                                                      List<MultipartFile> variantImagesList) {
        Map<Integer, ProductVariantRequest> variantMap = new LinkedHashMap<>();

        for (String key : params.keySet()) {
            if (key.startsWith("variant_price_")) {
                String indexStr = key.substring("variant_price_".length());
                try {
                    int variantIndex = Integer.parseInt(indexStr);

                    String sku = params.get("variant_sku_" + variantIndex);
                    String priceStr = params.get("variant_price_" + variantIndex);
                    String stockStr = params.get("variant_stock_" + variantIndex);

                    if (priceStr != null && !priceStr.isBlank() && stockStr != null && !stockStr.isBlank()) {
                        ProductVariantRequest variant = new ProductVariantRequest();
                        variant.setSku(sku != null && !sku.isBlank() ? sku : null);
                        variant.setPrice(new BigDecimal(priceStr));
                        variant.setStock(Integer.parseInt(stockStr));

                        // Parse attributes
                        List<ProductVariantAttributeValueRequest> attributes = new ArrayList<>();
                        for (String key2 : params.keySet()) {
                            if (key2.startsWith("variant_attr_" + variantIndex + "_")) {
                                String attrName = key2.substring(("variant_attr_" + variantIndex + "_").length());
                                String attrValue = params.get(key2);

                                if (attrValue != null && !attrValue.isBlank()) {
                                    attributes.add(new ProductVariantAttributeValueRequest(
                                            attrName,
                                            attrValue,
                                            attrValue
                                    ));
                                }
                            }
                        }
                        variant.setAttributes(attributes);

                        // ✅ Add images (empty list for now, handle in form)
                        List<ProductImageRequest> images = new ArrayList<>();
                        if (variantImagesList != null) {
                            for (MultipartFile file : variantImagesList) {
                                if (file != null && !file.isEmpty()) {
                                    // Kiểm tra file có phải của variant này không
                                    if (file.getOriginalFilename() != null) {
                                        String imageUrl = cloudinaryService.uploadImage(file);
                                        images.add(new ProductImageRequest(imageUrl));
                                    }
                                }
                            }
                        }
                        variant.setImages(images);

                        variantMap.put(variantIndex, variant);
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid index
                }
            }
        }

        return new ArrayList<>(variantMap.values());
    }
}