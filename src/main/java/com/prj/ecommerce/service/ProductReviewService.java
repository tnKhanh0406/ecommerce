package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.ProductReviewRequest;
import com.prj.ecommerce.dto.request.ReviewReplyRequest;
import com.prj.ecommerce.dto.request.UpdateReplyRequest;
import com.prj.ecommerce.dto.request.UpdateReviewRequest;
import com.prj.ecommerce.dto.response.ProductReviewResponse;
import com.prj.ecommerce.dto.response.ReviewReplyResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductReviewService {
    ProductReviewResponse getReviewByOrderItem(Long orderItemId);
    ProductReviewResponse createReview(ProductReviewRequest request, List<MultipartFile> images);
    ProductReviewResponse updateReview(UpdateReviewRequest request, List<MultipartFile> images);
    ReviewReplyResponse createReply(ReviewReplyRequest request);
    ReviewReplyResponse updateReply(Long replyId, UpdateReplyRequest request);
}
