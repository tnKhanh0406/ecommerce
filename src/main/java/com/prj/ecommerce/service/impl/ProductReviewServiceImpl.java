package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.ImageType;
import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.dto.request.ProductImageRequest;
import com.prj.ecommerce.dto.request.ProductReviewRequest;
import com.prj.ecommerce.dto.request.UpdateReviewRequest;
import com.prj.ecommerce.dto.response.ProductReviewResponse;
import com.prj.ecommerce.entity.*;
import com.prj.ecommerce.exception.BadRequestException;
import com.prj.ecommerce.model.UserPrincipal;
import com.prj.ecommerce.repository.*;
import com.prj.ecommerce.service.ProductReviewService;
import com.prj.ecommerce.utils.VariantUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {

    private final UserRepository userRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductImageRepository productImageRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    private UserEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private Long getCurrentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal())
                .getUserEntity().getId();
    }

    @Override
    @Transactional
    public ProductReviewResponse createReview(ProductReviewRequest request) {
        ProductReviewEntity review = new ProductReviewEntity();
        OrderItemEntity orderItem = orderItemRepository.findById(request.getOrderItemId())
                        .orElseThrow(() -> new EntityNotFoundException("Order item not found"));

        OrderEntity order = orderItem.getOrder();

        if (!order.getUser().getId().equals(getCurrentUserId())) {
            throw new AccessDeniedException("You do not have permission to review this item");
        }

        if (!canReview(order, 10)) {
            throw new BadRequestException("You cannot review this item");
        }

        if (productReviewRepository.existsByOrderItem_Id(orderItem.getId())) {
            throw new BadRequestException("This item has already been reviewed");
        }

        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Invalid rating");
        }

        ProductVariantEntity variant = orderItem.getProductVariant();
        ProductEntity product = variant.getProduct();

        review.setProduct(product);
        review.setOrderItem(orderItem);
        review.setUser(getCurrentUser());
        review.setShop(product.getShop());
        review.setComment(request.getComment());
        review.setRating(request.getRating());
        review.setVariantSnapshot(VariantUtil.generateVariantName(variant));

        int newCount = product.getReviewCount() + 1;
        BigDecimal newRating = product.getRating()
                .multiply(BigDecimal.valueOf(product.getReviewCount()))
                .add(BigDecimal.valueOf(request.getRating())).
                divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);
        product.setReviewCount(newCount);
        product.setRating(newRating);
        productRepository.save(product);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            createImages(request.getImages(), review);
        }
        productReviewRepository.save(review);

        return ProductReviewResponse.fromEntity(review);
    }

    @Override
    public ProductReviewResponse updateReview(UpdateReviewRequest request) {
        ProductReviewEntity review = productReviewRepository.findById(request.getReviewId())
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));

        if (request.getRating() != null && (request.getRating() < 1 || request.getRating() > 5)) {
            throw new BadRequestException("Invalid rating");
        }

        if (!review.getUser().getId().equals(getCurrentUserId())) {
            throw new AccessDeniedException("You do not have permission to review this item");
        }

        if (!canUpdate(review, 5)) {
            throw new BadRequestException("You cannot update this review");
        }

        review.setComment(request.getComment());
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            updateImages(request.getImages(), review);
        }
        productReviewRepository.save(review);
        return ProductReviewResponse.fromEntity(review);
    }

    private boolean canReview(OrderEntity order, Integer day) {
        OrderStatusHistoryEntity history = orderStatusHistoryRepository.findByOrder_IdAndToStatus(order.getId(), OrderStatus.COMPLETED);
        if (history == null) {
            return false;
        }
        return order.getOrderStatus() == OrderStatus.COMPLETED
                && Duration.between(history.getCreatedAt(), LocalDateTime.now()).toDays() <= day;
    }

    private void createImages(List<ProductImageRequest> images, ProductReviewEntity review) {
        for (ProductImageRequest imageRequest : images) {
            ProductImageEntity image = new ProductImageEntity();
            image.setProduct(review.getProduct());
            image.setVariant(review.getOrderItem().getProductVariant());
            image.setImageType(ImageType.REVIEW);
            image.setImageUrl(imageRequest.getImageUrl());
            image.setReview(review);
            review.getImages().add(image);
        }
    }

    private boolean canUpdate(ProductReviewEntity review, Integer day) {
        return Duration.between(review.getCreatedAt(), LocalDateTime.now()).toDays() <= day;
    }

    private void updateImages(List<ProductImageRequest> images, ProductReviewEntity review) {
        Set<String> newImageUrls = images.stream()
                .map(ProductImageRequest::getImageUrl)
                .collect(Collectors.toSet());

        List<ProductImageEntity> currentImages = productImageRepository.findAllByProduct_IdAndImageType(review.getProduct().getId(), ImageType.REVIEW);

        Set<String> currentImageUrls = currentImages.stream()
                .map(ProductImageEntity::getImageUrl)
                .collect(Collectors.toSet());

        for (ProductImageEntity image : currentImages) {
            if (!newImageUrls.contains(image.getImageUrl())) {
                review.getImages().remove(image);
            }
        }

        for (ProductImageRequest imageRequest : images) {
            if (!currentImageUrls.contains(imageRequest.getImageUrl())) {
                ProductImageEntity image = new ProductImageEntity();
                image.setReview(review);
                image.setImageType(ImageType.REVIEW);
                image.setImageUrl(imageRequest.getImageUrl());
                image.setProduct(review.getProduct());
                image.setVariant(review.getOrderItem().getProductVariant());
                review.getImages().add(image);
            }
        }
    }
}
