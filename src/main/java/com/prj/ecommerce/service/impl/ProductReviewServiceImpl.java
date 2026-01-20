package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.ImageType;
import com.prj.ecommerce.common.NotificationType;
import com.prj.ecommerce.common.ReferenceType;
import com.prj.ecommerce.dto.request.*;
import com.prj.ecommerce.dto.response.ProductReviewResponse;
import com.prj.ecommerce.dto.response.ReviewReplyResponse;
import com.prj.ecommerce.entity.*;
import com.prj.ecommerce.exception.BadRequestException;
import com.prj.ecommerce.exception.ConcurrentUpdateException;
import com.prj.ecommerce.model.UserPrincipal;
import com.prj.ecommerce.repository.*;
import com.prj.ecommerce.service.CloudinaryService;
import com.prj.ecommerce.service.NotificationService;
import com.prj.ecommerce.service.ProductReviewService;
import com.prj.ecommerce.service.ReviewPolicyService;
import com.prj.ecommerce.utils.VariantUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final ReviewReplyRepository reviewReplyRepository;
    private final NotificationService notificationService;
    private final ReviewPolicyService reviewPolicyService;
    private final CloudinaryService cloudinaryService;

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
    public ProductReviewResponse getReviewByOrderItem(Long orderItemId) {
        return ProductReviewResponse.fromEntity(productReviewRepository.findByOrderItem_Id(orderItemId));
    }

    @Override
    @Transactional
    public ProductReviewResponse createReview(ProductReviewRequest request, List<MultipartFile> images) {
        ProductReviewEntity review = new ProductReviewEntity();
        OrderItemEntity orderItem = orderItemRepository.findById(request.getOrderItemId())
                        .orElseThrow(() -> new EntityNotFoundException("Order item not found"));

        OrderEntity order = orderItem.getOrder();

        if (!order.getUser().getId().equals(getCurrentUserId())) {
            throw new AccessDeniedException("You do not have permission to review this item");
        }

        if (!reviewPolicyService.canReview(order, 10)) {
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

        updateProductRatingWithRetry(product.getId(), 0, request.getRating(), true);
        List<String> imageUrls = cloudinaryService.uploadImages(images);
        createImages(imageUrls, review);
        productReviewRepository.save(review);

        //Gui thong bao
        NotificationRequest notificationRequest = new NotificationRequest(
                "Khách hàng đánh giá sản phẩm",
                "Khách hàng " + review.getUser().getFullName() + " đã đánh giá sản phẩm " + review.getProduct().getName(),
                NotificationType.NEW_REVIEW,
                review.getId(),
                review.getShop().getUser().getId(),
                ReferenceType.REVIEW
        );

        notificationService.sendNotification(notificationRequest);

        return ProductReviewResponse.fromEntity(review);
    }

    @Override
    @Transactional
    public ProductReviewResponse updateReview(UpdateReviewRequest request) {
        ProductReviewEntity review = productReviewRepository.findById(request.getReviewId())
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));

        Integer newRating = request.getRating();
        if (newRating != null && (newRating < 1 || newRating > 5)) {
            throw new BadRequestException("Invalid rating");
        }

        if (!review.getUser().getId().equals(getCurrentUserId())) {
            throw new AccessDeniedException("You do not have permission to review this item");
        }

        if (!reviewPolicyService.canUpdate(review, 5)) {
            throw new BadRequestException("You cannot update this review");
        }

        int oldRating = review.getRating();

        if (newRating != null && newRating != oldRating) {
            updateProductRatingWithRetry(review.getProduct().getId(), oldRating, newRating, false);
            review.setRating(newRating);
        }

        review.setComment(request.getComment());
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            updateImages(request.getImages(), review);
        }
        productReviewRepository.save(review);
        return ProductReviewResponse.fromEntity(review);
    }

    private void updateProductRatingWithRetry(Long productId, int oldRating, int newRating, boolean isCreate) {
        int retry = 3;

        while (retry-- > 0) {
            try {
                ProductEntity product = productRepository.findById(productId)
                        .orElseThrow(() -> new EntityNotFoundException("Product not found"));

                int reviewCount = product.getReviewCount();
                BigDecimal rating = product.getRating();

                BigDecimal totalScore = rating.multiply(BigDecimal.valueOf(reviewCount));

                if (isCreate) {
                    reviewCount++;
                    totalScore = totalScore.add(BigDecimal.valueOf(newRating));
                } else {
                    // update review
                    totalScore = totalScore
                            .subtract(BigDecimal.valueOf(oldRating))
                            .add(BigDecimal.valueOf(newRating));
                }
                BigDecimal newAvg = totalScore
                        .divide(BigDecimal.valueOf(reviewCount), 2, RoundingMode.HALF_UP);

                product.setReviewCount(reviewCount);
                product.setRating(newAvg);

                productRepository.saveAndFlush(product);
                return; // SUCCESS
            } catch (ObjectOptimisticLockingFailureException e) {
                if (retry == 0) {
                    throw new ConcurrentUpdateException("Too many concurrent reviews, please retry");
                }
            }
        }
    }

    @Override
    public ReviewReplyResponse createReply(ReviewReplyRequest request) {
        ProductReviewEntity review = productReviewRepository.findById(request.getReviewId())
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        if (review.getReply() != null) {
            throw new BadRequestException("Review already replied");
        }

        if (!review.getProduct().getShop().getUser().getId().equals(getCurrentUserId())) {
            throw new AccessDeniedException("You cannot reply to this review");
        }

        ReviewReplyEntity reply = new ReviewReplyEntity();
        reply.setContent(request.getContent());
        reply.setSeller(getCurrentUser());
        reply.setReview(review);

        review.setReply(reply);
        reviewReplyRepository.save(reply);

        //Gui thong bao
        NotificationRequest notificationRequest = new NotificationRequest(
                "Phản hồi từ người bán",
                "Người bán đã phản hồi đánh giá của bạn",
                NotificationType.REVIEW_REPLY,
                reply.getId(),
                reply.getSeller().getId(),
                ReferenceType.REVIEW
        );

        notificationService.sendNotification(notificationRequest);

        return ReviewReplyResponse.fromEntity(reply);
    }

    @Override
    public ReviewReplyResponse updateReply(Long replyId, UpdateReplyRequest request) {
        ReviewReplyEntity reply = reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new EntityNotFoundException("Reply not found"));

        if (!reply.getReview().getProduct().getShop().getUser().getId().equals(getCurrentUserId())) {
            throw new AccessDeniedException("You cannot reply to this review");
        }
        reply.setContent(request.getContent());
        reviewReplyRepository.save(reply);
        return ReviewReplyResponse.fromEntity(reply);
    }

    private void createImages(List<String> urls, ProductReviewEntity review) {
        for (String url : urls) {
            ProductImageEntity image = new ProductImageEntity();
            image.setProduct(review.getProduct());
            image.setVariant(review.getOrderItem().getProductVariant());
            image.setImageType(ImageType.REVIEW);
            image.setImageUrl(url);
            image.setReview(review);
            review.getImages().add(image);
        }
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
