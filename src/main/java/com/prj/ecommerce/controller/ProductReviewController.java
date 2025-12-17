package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.ProductReviewRequest;
import com.prj.ecommerce.dto.request.ReviewReplyRequest;
import com.prj.ecommerce.dto.request.UpdateReplyRequest;
import com.prj.ecommerce.dto.request.UpdateReviewRequest;
import com.prj.ecommerce.dto.response.ProductReviewResponse;
import com.prj.ecommerce.dto.response.ReviewReplyResponse;
import com.prj.ecommerce.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ProductReviewController {
    private final ProductReviewService productReviewService;

    @PostMapping
    public ResponseEntity<ProductReviewResponse> review(@Valid @RequestBody ProductReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productReviewService.createReview(request));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ProductReviewResponse> updateReview(@Valid @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(productReviewService.updateReview(request));
    }

    @PostMapping("/reply")
    public ResponseEntity<ReviewReplyResponse> reply(@Valid @RequestBody ReviewReplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productReviewService.createReply(request));
    }

    @PutMapping("/reply/{replyId}")
    public ResponseEntity<ReviewReplyResponse> updateReply(@Valid @RequestBody UpdateReplyRequest request,
                                                           @PathVariable Long replyId) {
        return ResponseEntity.ok(productReviewService.updateReply(replyId, request));
    }
}
