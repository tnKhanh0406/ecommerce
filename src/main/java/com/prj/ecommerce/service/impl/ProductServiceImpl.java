package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.ImageType;
import com.prj.ecommerce.dto.request.*;
import com.prj.ecommerce.dto.response.*;
import com.prj.ecommerce.entity.*;
import com.prj.ecommerce.repository.*;
import com.prj.ecommerce.service.CategoryService;
import com.prj.ecommerce.service.CloudinaryService;
import com.prj.ecommerce.service.ProductService;
import com.prj.ecommerce.specification.ProductSpecification;
import com.prj.ecommerce.utils.SkuUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
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
    private final ProductVariantAttributeValueRepository productVariantAttributeValueRepository;
    private final ProductReviewRepository productReviewRepository;
    private final CategoryService categoryService;
    private final CloudinaryService cloudinaryService;

    private UserEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public Page<CreateProductResponse> getProducts(ProductFilterRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize()
        );

        List<Long> categoryIds = new ArrayList<>();
        if (request.getCategoryId() != null) {
            categoryIds = categoryService.getAllCategoryIds(request.getCategoryId());
        }

        Page<ProductEntity> productPage = productRepo.findAll(
                ProductSpecification.search(request, categoryIds),
                pageable
        );

        List<CreateProductResponse> responses = attachPriceRange(productPage.getContent());

        return new PageImpl<>(
                responses,
                pageable,
                productPage.getTotalElements()
        );
    }

    @Override
    @Cacheable(value = "trending")
    public List<CreateProductResponse> getRecommendProducts() {
        List<ProductEntity> products = productRepo.findRandomProducts(PageRequest.of(0, 30));

        return attachPriceRange(products);
    }

    @Override
    @Cacheable(value = "product", key = "#id")
    public ProductDetailResponse getProductDetail(Long id) {
        ProductEntity product = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        ProductPriceRangeResponse priceRange = productVariantRepository.findPriceRangeByProductId(product.getId())
                .orElseThrow(() -> new EntityNotFoundException("Product price range not found"));

        List<ProductVariantResponse> variantResponses = product.getVariants().stream()
                .map(ProductVariantResponse::fromEntity)
                .toList();

        List<ProductReviewEntity> reviews = productReviewRepository.findByProduct_Id(product.getId());
        List<ProductReviewResponse> reviewResponses = new ArrayList<>();
        if (reviews != null) {
            reviewResponses = reviews.stream()
                    .map(ProductReviewResponse::fromEntity)
                    .toList();
        }

        CreateShopResponse shop = CreateShopResponse.fromEntity(product.getShop());
        shop.setTotalProducts(productRepo.countByShop_Id(shop.getId()));

        ProductDetailResponse detail = new ProductDetailResponse();
        detail.setId(product.getId());
        detail.setName(product.getName());
        detail.setDescription(product.getDescription());

        detail.setMinPrice(priceRange.getMinPrice());
        detail.setMaxPrice(priceRange.getMaxPrice());

        detail.setProductImages(getImages(product, ImageType.THUMBNAIL));
        detail.setVariantImages(getImages(product, ImageType.VARIANT));

        detail.setBreadcrumb(getBreadCrumbs(product));

        detail.setAttributes(getProductAttributes(product));

        detail.setVariants(variantResponses);

        detail.setSoldCount(product.getSoldCount());
        detail.setRating(product.getRating());
        detail.setReviewCount(product.getReviewCount());

        detail.setShop(shop);

        detail.setReviews(reviewResponses);
        return detail;
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
        if (request.getImages() != null && !request.getImages().isEmpty()) {
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
    public CreateProductResponse createProductWithImages(CreateProductRequest request,
                                                         List<MultipartFile> productImages,
                                                         Map<String, List<MultipartFile>> variantImageMap) {
        attachProductImages(request, productImages);
        attachVariantImages(request.getVariants(), variantImageMap);
        return createProduct(request);
    }

    @Override
    @Transactional
    @CacheEvict(value = "product", key = "#productId")
    public CreateProductResponse updateBasicProductWithImages(Long productId,
                                                              UpdateBasicProductRequest request,
                                                              List<MultipartFile> productImages,
                                                              List<String> existingProductImageUrls) {
        List<ProductImageRequest> mergedImages = new ArrayList<>();

        if (existingProductImageUrls != null) {
            for (String imageUrl : existingProductImageUrls) {
                if (imageUrl != null && !imageUrl.isBlank()) {
                    mergedImages.add(new ProductImageRequest(imageUrl));
                }
            }
        }

        mergedImages.addAll(uploadImages(productImages));
        request.setImages(mergedImages);

        return updateBasicProduct(productId, request);
    }

    @Override
    @Transactional
    @CacheEvict(value = "product", key = "#productId")
    public ProductVariantListResponse updateBasicProductVariantWithImages(Long productId,
                                                                           ProductVariantListRequest request,
                                                                           Map<String, List<MultipartFile>> variantImageMap,
                                                                           Map<Integer, List<String>> existingVariantImageUrls) {
        attachVariantUpdateImages(request.getProductVariants(), variantImageMap, existingVariantImageUrls);
        return updateBasicProductVariant(productId, request);
    }

    @Override
    @Transactional
    @CacheEvict(value = "product", key = "#productId")
    public CreateProductResponse updateAttributeWithImages(Long productId,
                                                           UpdateAttributeRequest updateAttributeRequest,
                                                           Map<String, List<MultipartFile>> variantImageMap) {
        attachVariantCreateImages(updateAttributeRequest.getVariants(), variantImageMap);
        return updateAttribute(productId, updateAttributeRequest);
    }

    @Override
    @Transactional
    @CacheEvict(value = "product", key = "#productId")
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
    @CacheEvict(value = "product", key = "#productId")
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
    @CacheEvict(value = "product", key = "#productId")
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
    @CacheEvict(value = "product", key = "#productId")
    public void deleteProduct(Long productId) {
        ProductEntity product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        if (!product.getShop().getUser().getId().equals(getCurrentUser().getId())) {
            throw new AccessDeniedException("This product not belong to you");
        }
        productCategoryRepo.deleteAllByProduct_Id(productId);
        productRepo.deleteById(productId);
    }

    @Override
    public Page<CreateProductResponse> getProductsByShopId(Long shopId, int page, int size) {
        UserEntity user = getCurrentUser();
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));

        // ✅ Verify ownership
        if (!shop.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("This shop does not belong to you");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductEntity> productPage = productRepo.findByShop_Id(shopId, pageable);

        List<CreateProductResponse> responses = attachPriceRange(productPage.getContent());

        return new PageImpl<>(
                responses,
                pageable,
                productPage.getTotalElements()
        );
    }

    @Override
    public ProductDetailResponse getProductForEdit(Long productId) {
        ProductEntity product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        UserEntity user = getCurrentUser();
        if (!product.getShop().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("This product does not belong to you");
        }

        return getProductDetail(productId);
    }

    @Override
    public List<Long> getProductCategoryIds(Long productId) {
        ProductEntity product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        UserEntity user = getCurrentUser();
        if (!product.getShop().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("This product does not belong to you");
        }

        return productRepo.getCategoryIdsByProductId(productId);
    }

    @Override
    public List<Long> getCategoryIdsByShopId(Long shopId) {
        return productRepo.getCategoryIdsByShopId(shopId);
    }

    private List<CreateProductResponse> attachPriceRange(List<ProductEntity> products) {
        if (products.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = products.stream()
                .map(ProductEntity::getId)
                .collect(Collectors.toList());

        List<ProductPriceRangeResponse> priceRanges =
                productVariantRepository.findPriceRangeByProductIds(productIds);

        Map<Long, ProductPriceRangeResponse> priceMap =
                priceRanges.stream()
                        .collect(Collectors.toMap(
                                ProductPriceRangeResponse::getProductId,
                                Function.identity()
                        ));

        return products.stream().map(p -> {
            CreateProductResponse r = CreateProductResponse.fromEntity(p);
            ProductPriceRangeResponse price = priceMap.get(p.getId());
            if (price != null) {
                r.setMinPrice(price.getMinPrice());
                r.setMaxPrice(price.getMaxPrice());
            }
            return r;
        }).collect(Collectors.toList());
    }

    private List<ProductAttributeResponse> getProductAttributes(ProductEntity product) {
        List<ProductVariantAttributeValueEntity> vavs = productVariantAttributeValueRepository.findProductVariantAttributeValues(product.getId());
        Map<Long, ProductAttributeResponse> map = new LinkedHashMap<>();

        for (ProductVariantAttributeValueEntity vav : vavs) {
            ProductAttributeValueEntity value = vav.getAttributeValue();
            ProductAttributeEntity attribute = value.getProductAttribute();
            map.putIfAbsent(attribute.getId(), new ProductAttributeResponse(attribute.getId(), attribute.getName(), new ArrayList<>()));

            ProductAttributeResponse attr = map.get(attribute.getId());

            boolean exists = attr.getValues()
                    .stream()
                    .anyMatch(v -> v.getId().equals(value.getId()));
            if (!exists) {
                attr.getValues().add(new ProductAttributeValueResponse(value.getId(), value.getValue(), vav.getDisplayName()));
            }
        }
        return new ArrayList<>(map.values());
    }

    private List<ProductImageResponse> getImages(ProductEntity product, ImageType imageType) {
        List<ProductImageEntity> entities = productImageRepository.findAllByProduct_IdAndImageType(product.getId(), imageType);
        return entities.stream()
                .map(ProductImageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private List<CategoryResponse> getBreadCrumbs(ProductEntity product) {
        Long categoryId = productRepo.getCategoryIdsByProductId(product.getId()).get(0);
        List<CategoryResponse> breadCrumbs = new ArrayList<>();
        CategoryEntity category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        while (category != null) {
            breadCrumbs.add(CategoryResponse.fromEntity(category));
            category = category.getParent();
        }
        Collections.reverse(breadCrumbs);
        return breadCrumbs;
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
        if (images == null) {
            return;
        }

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

    private void attachProductImages(CreateProductRequest request, List<MultipartFile> productImages) {
        List<ProductImageRequest> uploadedImages = request.getImages() == null
                ? new ArrayList<>()
                : new ArrayList<>(request.getImages());

        if (productImages != null) {
            for (MultipartFile file : productImages) {
                if (file != null && !file.isEmpty()) {
                    uploadedImages.add(new ProductImageRequest(cloudinaryService.uploadImage(file)));
                }
            }
        }

        request.setImages(uploadedImages);
    }

    private void attachVariantImages(List<ProductVariantRequest> variants,
                                     Map<String, List<MultipartFile>> variantImageMap) {
        if (variants == null || variants.isEmpty()) {
            return;
        }

        for (int i = 0; i < variants.size(); i++) {
            ProductVariantRequest variant = variants.get(i);
            List<ProductImageRequest> uploadedImages = variant.getImages() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(variant.getImages());

            if (variantImageMap != null) {
                List<MultipartFile> files = variantImageMap.get("variantImages_" + i);
                if (files != null) {
                    for (MultipartFile file : files) {
                        if (file != null && !file.isEmpty()) {
                            uploadedImages.add(new ProductImageRequest(cloudinaryService.uploadImage(file)));
                        }
                    }
                }
            }

            variant.setImages(uploadedImages);
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
        if (images == null || images.isEmpty()) {
            return;
        }

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

    private List<ProductImageRequest> uploadImages(List<MultipartFile> files) {
        List<ProductImageRequest> images = new ArrayList<>();
        if (files == null) {
            return images;
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                images.add(new ProductImageRequest(cloudinaryService.uploadImage(file)));
            }
        }
        return images;
    }

    private void attachVariantUpdateImages(List<UpdateProductVariantRequest> variants,
                                           Map<String, List<MultipartFile>> variantImageMap,
                                           Map<Integer, List<String>> existingVariantImageUrls) {
        if (variants == null || variants.isEmpty()) {
            return;
        }

        for (int i = 0; i < variants.size(); i++) {
            List<ProductImageRequest> images = new ArrayList<>();

            if (existingVariantImageUrls != null) {
                List<String> existingUrls = existingVariantImageUrls.get(i);
                if (existingUrls != null) {
                    for (String url : existingUrls) {
                        if (url != null && !url.isBlank()) {
                            images.add(new ProductImageRequest(url));
                        }
                    }
                }
            }

            List<MultipartFile> files = variantImageMap == null ? null : variantImageMap.get("variantImages_" + i);
            if (files != null && !files.isEmpty()) {
                images.addAll(uploadImages(files));
            }

            variants.get(i).setImages(images);
        }
    }

    private void attachVariantCreateImages(List<ProductVariantRequest> variants,
                                           Map<String, List<MultipartFile>> variantImageMap) {
        if (variants == null || variants.isEmpty() || variantImageMap == null) {
            return;
        }

        for (int i = 0; i < variants.size(); i++) {
            List<ProductImageRequest> images = variants.get(i).getImages() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(variants.get(i).getImages());
            List<MultipartFile> files = variantImageMap.get("variantImages_" + i);
            if (files != null && !files.isEmpty()) {
                images.addAll(uploadImages(files));
            }
            variants.get(i).setImages(images);
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
