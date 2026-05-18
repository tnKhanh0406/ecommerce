package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.attribute.ProductAttributeRequest;
import com.prj.ecommerce.dto.request.attribute.ProductAttributeValueRequest;
import com.prj.ecommerce.dto.request.attribute.UpdateAttributeRequest;
import com.prj.ecommerce.dto.request.image.ProductImageRequest;
import com.prj.ecommerce.dto.request.product.CreateProductRequest;
import com.prj.ecommerce.dto.request.product.UpdateBasicProductRequest;
import com.prj.ecommerce.dto.request.review.ReviewReplyRequest;
import com.prj.ecommerce.dto.request.review.UpdateReplyRequest;
import com.prj.ecommerce.dto.request.shop.CreateShopRequest;
import com.prj.ecommerce.dto.request.shop.UpdateShopRequest;
import com.prj.ecommerce.dto.request.variant.ProductVariantAttributeValueRequest;
import com.prj.ecommerce.dto.request.variant.ProductVariantListRequest;
import com.prj.ecommerce.dto.request.variant.ProductVariantRequest;
import com.prj.ecommerce.dto.request.variant.UpdateProductVariantRequest;
import com.prj.ecommerce.dto.response.category.CategorySelectOptionResponse;
import com.prj.ecommerce.dto.response.category.CategoryTreeResponse;
import com.prj.ecommerce.dto.response.product.CreateProductResponse;
import com.prj.ecommerce.dto.response.product.ProductDetailResponse;
import com.prj.ecommerce.dto.response.shop.ShopResponse;
import com.prj.ecommerce.service.CategoryService;
import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.service.OrderService;
import com.prj.ecommerce.service.ProductService;
import com.prj.ecommerce.service.ProductReviewService;
import com.prj.ecommerce.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final ProductReviewService productReviewService;

    @GetMapping("/register")
    public String shopRegisterPage(Model model) {
        try {
            model.addAttribute("createShopRequest", new CreateShopRequest());
            return "shop/shopRegistration";

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
            return "shop/shopRegistration";
        }
        try {
            shopService.createShop(request, image);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký shop thành công!");
            return "redirect:/shop/dashboard";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "shop/shopRegistration";
        }
    }

    @GetMapping("/dashboard")
    public String shopDashboard(Model model) {
        try {
            model.addAttribute("shop", shopService.getCurrentUserShop());
            return "shop/shopDashboard";

        } catch (Exception e) {
            return "redirect:/shop/register";
        }
    }

    @GetMapping("/edit/{shopId}")
    public String editShopPage(Model model, @PathVariable Long shopId) {
        try {
            ShopResponse shop = shopService.getShopById(shopId);
            UpdateShopRequest shopRequest = new UpdateShopRequest(shop.getShopName(), shop.getDescription(), shop.getLogoUrl());
            model.addAttribute("updateShopRequest", shopRequest);
            model.addAttribute("shopId", shopId);
            return "shop/shopEdit";

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
            return "shop/shopEdit";
        }

        try {
            shopService.updateShop(shopId, request, logoFile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật shop thành công!");
            return "redirect:/shop/dashboard";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            model.addAttribute("shopId", shopId);
            return "shop/shopEdit";
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

            return "shop/shopProducts";

        } catch (Exception e) {
            return "redirect:/shop/dashboard";
        }
    }

    @GetMapping("/{shopId}/products/create")
    public String createProductPage(Model model,
                                    @PathVariable Long shopId) {
        try {
            List<CategorySelectOptionResponse> categoryOptions = new ArrayList<>();
            for (CategoryTreeResponse tree : categoryService.getCategoriesTree()) {
                flattenCategoryTree(tree, "", categoryOptions);
            }

            model.addAttribute("shop", shopService.getShopById(shopId));
            model.addAttribute("categoryOptions", categoryOptions);

            return "shop/shopProductCreate";

        } catch (Exception e) {
            return "redirect:/shop/dashboard";
        }
    }

    @GetMapping("/{shopId}/orders")
    public String shopOrdersPage(Model model,
                                 @PathVariable Long shopId,
                                 @RequestParam(required = false) OrderStatus status) {
        try {
            model.addAttribute("shop", shopService.getShopById(shopId));
            model.addAttribute("orders", orderService.getOrdersByShopId(shopId, status));
            model.addAttribute("currentStatus", status);
            return "shop/shopOrders";
        } catch (Exception e) {
            return "redirect:/shop/dashboard";
        }
    }

    @GetMapping("/{shopId}/analytics")
    public String shopAnalyticsPage(Model model,
                                    @PathVariable Long shopId,
                                    @RequestParam(required = false)
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                    @RequestParam(required = false)
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            LocalDate resolvedEndDate = endDate == null ? LocalDate.now() : endDate;
            LocalDate resolvedStartDate = startDate == null ? resolvedEndDate.minusDays(29) : startDate;

            model.addAttribute("shop", shopService.getShopById(shopId));
            model.addAttribute("analytics", orderService.getShopSalesAnalytics(shopId, resolvedStartDate, resolvedEndDate));
            model.addAttribute("startDate", resolvedStartDate);
            model.addAttribute("endDate", resolvedEndDate);
            return "shop/shopAnalytics";
        } catch (Exception e) {
            return "redirect:/shop/dashboard";
        }
    }

    @GetMapping("/{shopId}/reviews")
    public String shopReviewsPage(Model model,
                                  @PathVariable Long shopId) {
        try {
            model.addAttribute("shop", shopService.getShopById(shopId));
            model.addAttribute("reviews", productReviewService.getReviewsByShopId(shopId));
            return "shop/shopReviews";
        } catch (Exception e) {
            return "redirect:/shop/dashboard";
        }
    }

    @PostMapping("/{shopId}/reviews/{reviewId}/reply")
    public String createShopReviewReply(@PathVariable Long shopId,
                                        @PathVariable Long reviewId,
                                        @RequestParam("content") String content,
                                        RedirectAttributes redirectAttributes) {
        try {
            ReviewReplyRequest request = new ReviewReplyRequest();
            request.setReviewId(reviewId);
            request.setContent(content);
            productReviewService.createReply(request);
            redirectAttributes.addFlashAttribute("successMessage", "Phản hồi đánh giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể phản hồi: " + e.getMessage());
        }
        return String.format("redirect:/shop/%d/reviews", shopId);
    }

    @PostMapping("/{shopId}/reviews/{reviewId}/reply/{replyId}/update")
    public String updateShopReviewReply(@PathVariable Long shopId,
                                        @PathVariable Long reviewId,
                                        @PathVariable Long replyId,
                                        @RequestParam("content") String content,
                                        RedirectAttributes redirectAttributes) {
        try {
            UpdateReplyRequest request = new UpdateReplyRequest();
            request.setContent(content);
            productReviewService.updateReply(replyId, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phản hồi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật phản hồi: " + e.getMessage());
        }
        return String.format("redirect:/shop/%d/reviews", shopId);
    }

    @PostMapping("/{shopId}/orders/{orderId}/status")
    public String updateShopOrderStatus(@PathVariable Long shopId,
                                        @PathVariable Long orderId,
                                        @RequestParam("status") OrderStatus status,
                                        RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatusBySeller(orderId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật đơn hàng: " + e.getMessage());
        }

        String redirectUrl = String.format("redirect:/shop/%d/orders", shopId);
        if (status != null) {
            redirectUrl += "?status=" + status;
        }
        return redirectUrl;
    }

    @GetMapping("/products/{productId}/edit")
    public String editProductPage(Model model,
                                 @PathVariable Long productId) {
        try {
            ProductDetailResponse productDetail = productService.getProductForEdit(productId);
            List<CategorySelectOptionResponse> categoryOptions = new ArrayList<>();
            for (CategoryTreeResponse tree : categoryService.getCategoriesTree()) {
                flattenCategoryTree(tree, "", categoryOptions);
            }

            model.addAttribute("productDetail", productDetail);
            model.addAttribute("categoryOptions", categoryOptions);
            model.addAttribute("selectedCategoryIds", productService.getProductCategoryIds(productId));

            return "shop/productEdit";

        } catch (Exception e) {
            return "redirect:/shop/dashboard";
        }
    }

    @PostMapping("/products/{productId}/edit/basic")
    public String updateBasicProduct(@PathVariable Long productId,
                                     @RequestParam String name,
                                     @RequestParam String description,
                                     @RequestParam List<Long> categoryIds,
                                     MultipartHttpServletRequest multipartRequest,
                                     RedirectAttributes redirectAttributes) {
        try {
            UpdateBasicProductRequest request = new UpdateBasicProductRequest();
            request.setName(name);
            request.setDescription(description);
            request.setCategoryIds(categoryIds);
                List<String> existingProductImageUrls = parseExistingProductImageUrls(multipartRequest.getParameterMap());
                List<MultipartFile> newProductImages = multipartRequest.getFiles("productImages");

            productService.updateBasicProductWithImages(
                productId,
                request,
                newProductImages,
                existingProductImageUrls
            );

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cơ bản thành công!");
            return String.format("redirect:/shop/products/%d/edit", productId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return String.format("redirect:/shop/products/%d/edit", productId);
        }
    }

    @PostMapping("/products/{productId}/edit/variants")
    public String updateBasicProductVariants(@PathVariable Long productId,
                                             @RequestParam Map<String, String> variantParams,
                                             MultipartHttpServletRequest multipartRequest,
                                             RedirectAttributes redirectAttributes) {
        try {
            Map<Integer, List<String>> existingVariantImageUrls = parseExistingVariantImageUrls(variantParams);
            ProductVariantListRequest request = parseVariantList(variantParams, existingVariantImageUrls);
            productService.updateBasicProductVariantWithImages(
                productId,
                request,
                multipartRequest.getMultiFileMap(),
                existingVariantImageUrls
            );

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật variant thành công!");
            return String.format("redirect:/shop/products/%d/edit", productId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return String.format("redirect:/shop/products/%d/edit", productId);
        }
    }

    @PostMapping("/products/{productId}/edit/attributes")
    public String updateAttributeVariants(@PathVariable Long productId,
                                          @RequestParam Map<String, String> variantParams,
                                          MultipartHttpServletRequest multipartRequest,
                                          RedirectAttributes redirectAttributes) {
        try {
            Map<Integer, List<String>> existingVariantImageUrls = parseExistingVariantImageUrls(variantParams);
            UpdateAttributeRequest request = new UpdateAttributeRequest();
            request.setAttributes(parseAttributes(variantParams));
            request.setVariants(parseVariants(variantParams, existingVariantImageUrls));

            productService.updateAttributeWithImages(productId, request, multipartRequest.getMultiFileMap());

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thêm/xóa variant thành công!");
            return String.format("redirect:/shop/products/%d/edit", productId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return String.format("redirect:/shop/products/%d/edit", productId);
        }
    }

    @PostMapping("/{shopId}/products/create")
    public String createProduct(@RequestParam String name,
                                @RequestParam String description,
                                @RequestParam List<Long> categoryIds,
                                @RequestParam(required = false) List<MultipartFile> productImages,
                                @RequestParam Map<String, String> attributeParams,
                                @RequestParam Map<String, String> variantParams,
                                @PathVariable Long shopId,
                                MultipartHttpServletRequest multipartRequest,
                                RedirectAttributes redirectAttributes) {
        try {
            List<ProductAttributeRequest> attributes = parseAttributes(attributeParams);

            List<ProductVariantRequest> variants = parseVariants(variantParams, Collections.emptyMap());

            CreateProductRequest request = new CreateProductRequest();
            request.setShopId(shopId);
            request.setName(name);
            request.setDescription(description);
            request.setCategoryIds(categoryIds);
            request.setAttributes(attributes);
            request.setVariants(variants);

            productService.createProductWithImages(request, productImages, multipartRequest.getMultiFileMap());

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

    private void flattenCategoryTree(CategoryTreeResponse category,
                                     String prefix,
                                     List<CategorySelectOptionResponse> options) {
        options.add(new CategorySelectOptionResponse(category.getId(), prefix + category.getName()));

        if (category.getChildren() == null || category.getChildren().isEmpty()) {
            return;
        }

        String childPrefix = prefix + "  └ ";
        for (CategoryTreeResponse child : category.getChildren()) {
            flattenCategoryTree(child, childPrefix, options);
        }
    }

    private List<ProductVariantRequest> parseVariants(Map<String, String> params,
                                                      Map<Integer, List<String>> existingVariantImageUrls) {
        Map<Integer, ProductVariantRequest> variantMap = new TreeMap<>();

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

                        List<ProductImageRequest> images = new ArrayList<>();
                        if (existingVariantImageUrls != null) {
                            List<String> existingUrls = existingVariantImageUrls.get(variantIndex);
                            if (existingUrls != null) {
                                for (String url : existingUrls) {
                                    if (url != null && !url.isBlank()) {
                                        images.add(new ProductImageRequest(url));
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

    private ProductVariantListRequest parseVariantList(Map<String, String> params,
                                                       Map<Integer, List<String>> existingVariantImageUrls) {
        Map<Integer, UpdateProductVariantRequest> variantMap = new TreeMap<>();

        for (String key : params.keySet()) {
            if (key.startsWith("variant_price_")) {
                String indexStr = key.substring("variant_price_".length());
                try {
                    int variantIndex = Integer.parseInt(indexStr);
                    String idStr = params.get("variant_id_" + variantIndex);
                    String sku = params.get("variant_sku_" + variantIndex);
                    String priceStr = params.get("variant_price_" + variantIndex);
                    String stockStr = params.get("variant_stock_" + variantIndex);

                    if (idStr != null && !idStr.isBlank() && priceStr != null && !priceStr.isBlank() && stockStr != null && !stockStr.isBlank()) {
                        UpdateProductVariantRequest variant = new UpdateProductVariantRequest();
                        variant.setId(Long.parseLong(idStr));
                        variant.setSku(sku != null && !sku.isBlank() ? sku : null);
                        variant.setPrice(new BigDecimal(priceStr));
                        variant.setStock(Integer.parseInt(stockStr));
                        List<ProductImageRequest> images = new ArrayList<>();
                        if (existingVariantImageUrls != null) {
                            List<String> existingUrls = existingVariantImageUrls.get(variantIndex);
                            if (existingUrls != null) {
                                for (String url : existingUrls) {
                                    if (url != null && !url.isBlank()) {
                                        images.add(new ProductImageRequest(url));
                                    }
                                }
                            }
                        }
                        variant.setImages(images);
                        variantMap.put(variantIndex, variant);
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid row
                }
            }
        }

        ProductVariantListRequest request = new ProductVariantListRequest();
        request.setProductVariants(new ArrayList<>(variantMap.values()));
        return request;
    }

    private List<String> parseExistingProductImageUrls(Map<String, String[]> params) {
        List<Map.Entry<String, String[]>> entries = params.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("product_existing_image_"))
                .sorted(Map.Entry.comparingByKey())
                .toList();

        List<String> urls = new ArrayList<>();
        for (Map.Entry<String, String[]> entry : entries) {
            String[] values = entry.getValue();
            if (values == null) {
                continue;
            }
            for (String url : values) {
                if (url != null && !url.isBlank()) {
                    urls.add(url);
                }
            }
        }
        return urls;
    }

    private Map<Integer, List<String>> parseExistingVariantImageUrls(Map<String, String> params) {
        Map<Integer, List<String>> imageMap = new TreeMap<>();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("variant_existing_image_")) {
                continue;
            }

            String[] parts = key.split("_");
            if (parts.length < 5) {
                continue;
            }

            try {
                int variantIndex = Integer.parseInt(parts[3]);
                String url = entry.getValue();
                if (url != null && !url.isBlank()) {
                    imageMap.computeIfAbsent(variantIndex, ignored -> new ArrayList<>()).add(url);
                }
            } catch (NumberFormatException ignored) {
                // Skip malformed key
            }
        }

        return imageMap;
    }

}