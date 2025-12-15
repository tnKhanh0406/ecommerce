package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.ProductReviewRequest;
import com.prj.ecommerce.dto.response.ProductReviewResponse;

public interface ProductReviewService {
    ProductReviewResponse createReview(ProductReviewRequest productReviewRequest);
    ProductReviewResponse updateReview(ProductReviewRequest productReviewRequest);
}
