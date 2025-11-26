package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.ImageType;
import com.prj.ecommerce.dto.request.*;
import com.prj.ecommerce.dto.response.CreateProductResponse;
import com.prj.ecommerce.entity.*;
import com.prj.ecommerce.repository.*;
import com.prj.ecommerce.service.ProductService;
import com.prj.ecommerce.utils.SkuUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public CreateProductResponse createProduct(CreateProductRequest request) {
        //1. validate shop belong to seller
        ShopEntity shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        UserEntity user = getCurrentUser();
        if (!shop.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Shop does not belong to seller");
        }

        //2. create product
        ProductEntity product = new ProductEntity();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setShop(shop);
        productRepo.save(product);

        //3. set category
        if (request.getCategoryIds() != null) {
           for(Long categoryId : request.getCategoryIds()) {
               ProductCategoryEntity productCategory = new ProductCategoryEntity();
               productCategory.setProduct(product);
               CategoryEntity category = categoryRepo.findById(categoryId)
                       .orElseThrow(() -> new RuntimeException("Category not found")) ;
               productCategory.setCategory(category);
               productCategoryRepo.save(productCategory);
           }
        }

        // 4. Process attributes: create attribute if missing + values
        // Map attributeName -> attributeEntity
        Map<String, ProductAttributeEntity> attrMap = new HashMap<>();
        if (request.getAttributes() != null) {
            for (ProductAttributeRequest aReq : request.getAttributes()) {
                // find attribute: first seller-specific, then global
                ProductAttributeEntity attribute = attributeRepo.findByNameForSeller(aReq.getName(), user.getId())
                        .orElseGet(() -> {
                            ProductAttributeEntity attr = new ProductAttributeEntity();
                            attr.setName(aReq.getName());
                            // set seller
                            attr.setSeller(user);
                            return attributeRepo.save(attr);
                        });
                attrMap.put(attribute.getName(), attribute);

                // create values (if any)
                if (aReq.getValues() != null) {
                    for (ProductAttributeValueRequest v : aReq.getValues()) {
                        String val = v.getValue();
                        valueRepo.findByProductAttribute_IdAndValue(attribute.getId(), val)
                                .orElseGet(() -> {
                                    ProductAttributeValueEntity pav = new ProductAttributeValueEntity();
                                    pav.setValue(val);
                                    pav.setProductAttribute(attribute);
                                    return valueRepo.save(pav);
                                });
                    }
                }
            }
        }

        // 5. Process images at product level
        if (request.getImages() != null) {
            for (ProductImageRequest im : request.getImages()) {
                ProductImageEntity pi = new ProductImageEntity();
                pi.setImageUrl(im.getImageUrl());
                pi.setImageType(ImageType.THUMBNAIL);
                pi.setProduct(product);
                imageRepo.save(pi);
            }
        }

        // 6. Create variants
        if (request.getVariants() != null) {
            for (int i = 0; i < request.getVariants().size(); ++i) {
                ProductVariantRequest vr = request.getVariants().get(i);
                ProductVariantEntity variant = new ProductVariantEntity();
                variant.setProduct(product);
                variant.setPrice(vr.getPrice());
                variant.setStock(vr.getStock());
                variant.setSku(vr.getSku() == null || vr.getSku().isBlank()
                        ? SkuUtil.generateSku(product, i)
                        : vr.getSku());
                variant = variantRepo.save(variant);

                // save variant attribute values
                if (vr.getAttributes() != null) {
                    for (ProductVariantAttributeValueRequest pair : vr.getAttributes()) {
                        // find attribute by name in attrMap (if seller passed)
                        ProductAttributeEntity attr = attrMap.get(pair.getAttribute());
                        if (attr == null) {
                            // create attribute on the fly (seller-scope)
                            ProductAttributeEntity newAttr = new ProductAttributeEntity();
                            newAttr.setName(pair.getAttribute());
                            newAttr.setSeller(user);
                            attr = attributeRepo.save(newAttr);
                            attrMap.put(attr.getName(), attr);
                        }
                        // find or create attribute value
                        ProductAttributeEntity finalAttr = attr;
                        ProductAttributeValueEntity pav = valueRepo.findByProductAttribute_IdAndValue(attr.getId(), pair.getValue())
                                .orElseGet(() -> {
                                    ProductAttributeValueEntity newVal = new ProductAttributeValueEntity();
                                    newVal.setValue(pair.getValue());
                                    newVal.setProductAttribute(finalAttr);
                                    return valueRepo.save(newVal);
                                });
                        // create variant_attribute_value
                        ProductVariantAttributeValueEntity vv = new ProductVariantAttributeValueEntity();
                        vv.setVariant(variant);
                        vv.setAttributeValue(pav);
                        vv.setDisplayName(pair.getDisplayName());
                        variantAttrRepo.save(vv);
                    }
                }

                if (vr.getImages() != null) {
                    for (ProductImageRequest im : vr.getImages()) {
                        ProductImageEntity pi = new ProductImageEntity();
                        pi.setImageUrl(im.getImageUrl());
                        pi.setImageType(ImageType.VARIANT);
                        pi.setProduct(product);
                        pi.setVariant(variant);
                        imageRepo.save(pi);
                    }
                }
            }
        }
        return CreateProductResponse.from(product);
    }
}
