package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.ImageType;
import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.dto.request.ProductImageRequest;
import com.prj.ecommerce.dto.request.ProductReviewRequest;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public ProductReviewResponse createReview(ProductReviewRequest request) {
        UserEntity user = getCurrentUser();

        ProductReviewEntity review = new ProductReviewEntity();
        OrderItemEntity orderItem = orderItemRepository.findById(request.getOrderItemId())
                        .orElseThrow(() -> new EntityNotFoundException("Order item not found"));

        OrderEntity order = orderItem.getOrder();

        if (!order.getUser().getId().equals(user.getId())) {
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
        review.setUser(user);
        review.setShop(product.getShop());
        review.setComment(request.getComment());
        review.setRating(request.getRating());
        review.setVariantSnapshot(VariantUtil.generateVariantName(variant));

        productReviewRepository.save(review);

        int newCount = product.getReviewCount() + 1;
        BigDecimal newRating = product.getRating()
                .multiply(BigDecimal.valueOf(product.getReviewCount()))
                .add(BigDecimal.valueOf(request.getRating())).
                divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);
        product.setReviewCount(newCount);
        product.setRating(newRating);
        productRepository.save(product);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<ProductImageEntity> images = new ArrayList<>();
            for (ProductImageRequest imageRequest : request.getImages()) {
                ProductImageEntity image = new ProductImageEntity();
                image.setProduct(product);
                image.setVariant(variant);
                image.setImageType(ImageType.REVIEW);
                image.setImageUrl(imageRequest.getImageUrl());
                image.setReview(review);
                images.add(image);
            }

            productImageRepository.saveAll(images);
        }

        return ProductReviewResponse.fromEntity(review);
    }

    @Override
    public ProductReviewResponse updateReview(ProductReviewRequest request) {
        return null;
    }

    private boolean canReview(OrderEntity order, Integer day) {
        OrderStatusHistoryEntity history = orderStatusHistoryRepository.findByOrder_IdAndToStatus(order.getId(), OrderStatus.COMPLETED);
        if (history == null) {
            return false;
        }
        return order.getOrderStatus() == OrderStatus.COMPLETED
                && Duration.between(history.getCreatedAt(), LocalDateTime.now()).toDays() <= day;
    }
}
