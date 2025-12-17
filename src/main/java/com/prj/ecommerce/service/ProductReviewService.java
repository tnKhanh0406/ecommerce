package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.ProductReviewRequest;
import com.prj.ecommerce.dto.request.ReviewReplyRequest;
import com.prj.ecommerce.dto.request.UpdateReplyRequest;
import com.prj.ecommerce.dto.request.UpdateReviewRequest;
import com.prj.ecommerce.dto.response.ProductReviewResponse;
import com.prj.ecommerce.dto.response.ReviewReplyResponse;

public interface ProductReviewService {
    ProductReviewResponse createReview(ProductReviewRequest request);
    ProductReviewResponse updateReview(UpdateReviewRequest request);
    ReviewReplyResponse createReply(ReviewReplyRequest request);
    ReviewReplyResponse updateReply(Long replyId, UpdateReplyRequest request);
}
