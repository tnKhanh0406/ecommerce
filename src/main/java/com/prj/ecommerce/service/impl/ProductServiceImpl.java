package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.ImageType;
import com.prj.ecommerce.dto.request.*;
import com.prj.ecommerce.dto.response.CreateProductResponse;
import com.prj.ecommerce.entity.*;
import com.prj.ecommerce.repository.*;
import com.prj.ecommerce.service.ProductService;
import com.prj.ecommerce.utils.SkuUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final ProductAttributeRepository attributeRepo;
    private final ProductAttributeValueRepository valueRepo;
    private final ProductVariantAttributeValueRepository variantAttrRepo;
    private final ProductImageRepository imageRepo;
    private final ProductCategoryRepository productCategoryRepo;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepo;

    private UserEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public CreateProductResponse createProduct(CreateProductRequest request) {
        //1. validate shop belong to seller
        ShopEntity shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));
        UserEntity user = getCurrentUser();
        if (!shop.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Shop does not belong to seller");
        }

        //2. create product
        ProductEntity product = new ProductEntity();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setShop(shop);
        productRepo.save(product);

        //3. set category
        if (request.getCategoryIds() != null) {
           createProductCategories(product, request.getCategoryIds());
        }

        // 4. process attributes: create attribute if missing + values
        // Map attributeName -> attributeEntity
        Map<String, ProductAttributeEntity> attrMap = processProductAttributes(request.getAttributes(), user);

        // 5. create images at product level
        if (request.getImages() != null) {
            createProductImages(product, null, request.getImages());
        }

        // 6. create variants
        if (request.getVariants() != null) {
            createProductVariants(product, request.getVariants(), attrMap);
        }
        return CreateProductResponse.fromEntity(product);
    }

    private void createProductCategories(ProductEntity product, List<Long> categoryIds) {
        for (Long categoryId : categoryIds) {
            ProductCategoryEntity productCategory = new ProductCategoryEntity();
            productCategory.setProduct(product);
            CategoryEntity category = categoryRepo.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            productCategory.setCategory(category);
            productCategoryRepo.save(productCategory);
        }
    }

    private Map<String, ProductAttributeEntity> processProductAttributes(List<ProductAttributeRequest> attributeRequests,
                                                                         UserEntity user) {
        Map<String, ProductAttributeEntity> attrMap = new HashMap<>();

        if (attributeRequests == null || attributeRequests.isEmpty()) {
            return attrMap;
        }

        for (ProductAttributeRequest aReq : attributeRequests) {
            ProductAttributeEntity attribute = findOrCreateAttribute(aReq. getName(), user);
            attrMap.put(attribute.getName(), attribute);

            if (aReq.getValues() != null) {
                createAttributeValues(attribute, aReq.getValues());
            }
        }

        return attrMap;
    }

    private void createAttributeValues(ProductAttributeEntity attr, List<ProductAttributeValueRequest> attrValues) {
        for (ProductAttributeValueRequest attrValue : attrValues) {
            String val = attrValue.getValue();
            valueRepo.findByProductAttribute_IdAndValue(attr.getId(), val)
                    .orElseGet(() -> {
                        ProductAttributeValueEntity pav = new ProductAttributeValueEntity();
                        pav.setValue(val);
                        pav.setProductAttribute(attr);
                        return valueRepo.save(pav);
                    });
        }
    }

    private ProductAttributeEntity findOrCreateAttribute(String attributeName, UserEntity user) {
        return attributeRepo.findByNameForSeller(attributeName, user.getId())
                .orElseGet(() -> {
                    ProductAttributeEntity attr = new ProductAttributeEntity();
                    attr.setName(attributeName);
                    attr.setSeller(user);
                    return attributeRepo.save(attr);
                });
    }

    private void createProductImages(ProductEntity product, ProductVariantEntity variant, List<ProductImageRequest> images) {
        for (ProductImageRequest im : images) {
            ProductImageEntity image = new ProductImageEntity();
            image.setImageUrl(im.getImageUrl());
            image.setProduct(product);
            if (variant != null) {
                image.setVariant(variant);
                image.setImageType(ImageType.VARIANT);
            } else {
                image.setImageType(ImageType.THUMBNAIL);
            }
            imageRepo.save(image);
        }
    }

    private void createProductVariants(ProductEntity product,
                                       List<ProductVariantRequest> variantRequests,
                                       Map<String, ProductAttributeEntity> attrMap) {
        if (variantRequests == null || variantRequests.isEmpty()) {
            return;
        }

        for (int i = 0; i < variantRequests.size(); i++) {
            ProductVariantRequest vr = variantRequests.get(i);
            ProductVariantEntity variant = createVariant(product, vr, i);

            if (vr.getAttributes() != null) {
                createVariantAttributeValues(vr.getAttributes(), variant, attrMap);
            }

            if (vr.getImages() != null) {
                createProductImages(product, variant, vr. getImages());
            }
        }
    }

    private ProductVariantEntity createVariant(ProductEntity product, ProductVariantRequest request, int index) {
        ProductVariantEntity variant = new ProductVariantEntity();
        variant.setProduct(product);
        variant.setPrice(request.getPrice());
        variant.setStock(request.getStock());
        variant.setSku(request.getSku() == null || request.getSku().isBlank()
                ? SkuUtil.generateSku(product, index)
                : request.getSku());
        return variantRepo.save(variant);
    }

    private void createVariantAttributeValues(List<ProductVariantAttributeValueRequest> requests,
                                              ProductVariantEntity variant,
                                              Map<String, ProductAttributeEntity> attrMap) {
        for (ProductVariantAttributeValueRequest pair : requests) {
            ProductAttributeEntity attr = attrMap.get(pair.getAttribute());
            if (attr == null) {
                throw new EntityNotFoundException("Attribute not found: " + pair.getAttribute());
            }

            // Find attribute value
            ProductAttributeValueEntity pav = valueRepo
                    .findByProductAttribute_IdAndValue(attr.getId(), pair.getValue())
                    . orElseThrow(() -> new EntityNotFoundException(
                            "Value attribute not found: " + pair.getValue() + " for attribute: " + pair.getAttribute()));

            // Create variant_attribute_value
            ProductVariantAttributeValueEntity vv = new ProductVariantAttributeValueEntity();
            vv.setVariant(variant);
            vv.setAttributeValue(pav);
            vv.setDisplayName(pair.getDisplayName());
            variantAttrRepo.save(vv);
        }
    }
}
