package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.ImageType;
import com.prj.ecommerce.dto.request.*;
import com.prj.ecommerce.dto.response.CreateProductResponse;
import com.prj.ecommerce.dto.response.ProductVariantListResponse;
import com.prj.ecommerce.dto.response.ProductVariantResponse;
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

import java.util.*;
import java.util.stream.Collectors;
//product khong co attribute -> product image type dang la variant, dung ra phai la thumbnail
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
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;

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

    @Override
    @Transactional
    public CreateProductResponse updateBasicProduct(Long productId, UpdateBasicProductRequest request) {
        ProductEntity product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        if (!product.getShop().getUser().getId().equals(getCurrentUser().getId())) {
            throw new AccessDeniedException("This product not belong to you");
        }
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        productRepo.save(product);
        if (request.getCategoryIds() != null) {
            updateProductCategories(product, request.getCategoryIds());
        }
        if (request.getImages() != null) {
            updateProductImages(product, null, request.getImages());
        }
        return CreateProductResponse.fromEntity(product);
    }

    @Override
    @Transactional
    public ProductVariantListResponse updateBasicProductVariant(Long productId, ProductVariantListRequest request) {
        ProductEntity product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        if (!product.getShop().getUser().getId().equals(getCurrentUser().getId())) {
            throw new AccessDeniedException("This product not belong to you");
        }
        List<ProductVariantEntity> entities = productVariantRepository.findByProductId(productId);
        Map<Long, ProductVariantEntity> variantMap = entities.stream()
                        .collect(Collectors.toMap(ProductVariantEntity::getId, v -> v));
        for (UpdateProductVariantRequest dto : request.getProductVariants()) {
            ProductVariantEntity variant = variantMap.get(dto.getId());
            if (variant == null) {
                throw new AccessDeniedException("Variant id " + dto.getId() + " is not belong to this product");
            }

            variant.setSku(dto.getSku());
            variant.setPrice(dto.getPrice());
            variant.setStock(dto.getStock());

            // update images
            updateProductImages(product, variant, dto.getImages());

            productVariantRepository.save(variant);
        }
        return ProductVariantListResponse.fromEntity(entities);
    }

    @Override
    @Transactional
    public CreateProductResponse updateAttribute(Long productId, UpdateAttributeRequest updateAttributeRequest) {
        ProductEntity product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        if (!product.getShop().getUser().getId().equals(getCurrentUser().getId())) {
            throw new AccessDeniedException("This product not belong to you");
        }
        List<ProductVariantEntity> variants = productVariantRepository.findByProductId(productId);
        Map<String, ProductVariantEntity> oldVariantMap = buildVariantMap(variants);
        Map<String, ProductVariantRequest> newVariantMap = buildNewVariantMap(updateAttributeRequest.getVariants());

        //Update remain variants
        for (String key : newVariantMap.keySet()) {
            if (oldVariantMap.containsKey(key)) {

                ProductVariantEntity oldVariant = oldVariantMap.get(key);
                ProductVariantRequest newVariant = newVariantMap.get(key);

                oldVariant.setSku(newVariant.getSku());
                oldVariant.setPrice(newVariant.getPrice());
                oldVariant.setStock(newVariant.getStock());

                updateProductImages(product, oldVariant, newVariant.getImages());

                productVariantRepository.save(oldVariant);
            }
        }

        //Delete variants
        for (String key : oldVariantMap.keySet()) {
            if (!newVariantMap.containsKey(key)) {
                productVariantRepository.delete(oldVariantMap.get(key));
            }
        }

        //Insert new variants
        int i = 0;
        for (String key : newVariantMap.keySet()) {

            if (!oldVariantMap.containsKey(key)) {

                ProductVariantRequest newVariant = newVariantMap.get(key);

                ProductVariantEntity entity = new ProductVariantEntity();
                entity.setProduct(product);
                entity.setSku(newVariant.getSku() == null ? SkuUtil.generateSku(product, i) : newVariant.getSku());
                entity.setPrice(newVariant.getPrice());
                entity.setStock(newVariant.getStock());

                productVariantRepository.save(entity);

                Map<String, ProductAttributeEntity> attrMap = processProductAttributes(updateAttributeRequest.getAttributes(), getCurrentUser());
                createVariantAttributeValues(newVariant.getAttributes(), entity, attrMap);
                createProductImages(product, entity, newVariant.getImages());

                i++;
            }
        }

        return CreateProductResponse.fromEntity(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        ProductEntity product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        if (!product.getShop().getUser().getId().equals(getCurrentUser().getId())) {
            throw new AccessDeniedException("This product not belong to you");
        }
        productCategoryRepo.deleteAllByProduct_Id(productId);
        productRepo.deleteById(productId);
    }

    private Map<String, ProductVariantRequest> buildNewVariantMap(List<ProductVariantRequest> requests) {
        Map<String, ProductVariantRequest> newVariantMap = new HashMap<>();
        for (ProductVariantRequest request : requests) {
            newVariantMap.put(buildVariantKey(request.getAttributes()), request);
        }
        return newVariantMap;
    }

    private Map<String, ProductVariantEntity> buildVariantMap(List<ProductVariantEntity> variants) {
        Map<String, ProductVariantEntity> variantMap = new HashMap<>();
        for (ProductVariantEntity variant : variants) {
            List<ProductVariantAttributeValueRequest> attrs = variant.getAttributes()
                    .stream()
                    .map(v -> new ProductVariantAttributeValueRequest(
                            v.getAttributeValue().getProductAttribute().getName(),
                            v.getAttributeValue().getValue(),
                            v.getDisplayName()
                    ))
                    .toList();

            String key = buildVariantKey(attrs);
            variantMap.put(key, variant);
        }
        return variantMap;
    }

    private String buildVariantKey(List<ProductVariantAttributeValueRequest> requests) {
        return requests.stream()
                .sorted(Comparator.comparing(ProductVariantAttributeValueRequest::getAttribute))
                .map(a -> a.getAttribute() + "=" + a.getValue())
                .collect(Collectors.joining("|"));
    }

    private void updateProductImages(ProductEntity product, ProductVariantEntity variant, List<ProductImageRequest> images) {
        Set<String> newImageUrls = images.stream()
                .map(ProductImageRequest::getImageUrl)
                .collect(Collectors.toSet());
        List<ProductImageEntity> currentImages;
        if (variant == null) {
            currentImages = productImageRepository.findAllByProduct_IdAndImageType(product.getId(), ImageType.THUMBNAIL);
        } else {
            currentImages = productImageRepository.findAllByVariant_IdAndImageType(variant.getId(), ImageType.VARIANT);
        }
        Set<String> currentImageUrls = currentImages
                .stream()
                .map(ProductImageEntity::getImageUrl)
                .collect(Collectors.toSet());

        for(ProductImageEntity image : currentImages) {
            if(!newImageUrls.contains(image.getImageUrl())) {
                productImageRepository.delete(image);
            }
        }

        for(ProductImageRequest imgReq : images){
            if(!currentImageUrls.contains(imgReq.getImageUrl())){
                ProductImageEntity img = new ProductImageEntity();
                img.setImageUrl(imgReq.getImageUrl());
                img.setProduct(product);
                if (variant == null) {
                    img.setImageType(ImageType.THUMBNAIL);
                } else {
                    img.setImageType(ImageType.VARIANT);
                    img.setVariant(variant);
                }
                productImageRepository.save(img);
            }
        }
    }

    private void updateProductCategories(ProductEntity product, List<Long> categoryIds) {
        Set<Long> newCategoryIds = new HashSet<>(categoryIds);
        List<ProductCategoryEntity> current = productCategoryRepo.findAllByProduct_Id(product.getId());
        Set<Long> currentCategoryIds = current.stream()
                .map(pc -> pc.getCategory().getId())
                .collect(Collectors.toSet());

        for (ProductCategoryEntity pc : current) {
            if (!newCategoryIds.contains(pc.getCategory().getId())) {
                productCategoryRepo.delete(pc);
            }
        }

        for (Long categoryId : newCategoryIds) {
            if (!currentCategoryIds.contains(categoryId)) {
                CategoryEntity category = categoryRepo.findById(categoryId)
                        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
                ProductCategoryEntity pc = new ProductCategoryEntity();
                pc.setCategory(category);
                pc.setProduct(product);
                productCategoryRepo.save(pc);
            }
        }
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
