package com.prj.ecommerce.api;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ProductReviewApiController {
    private final ProductReviewService productReviewService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductReviewResponse> review(@Valid @ModelAttribute ProductReviewRequest request,
                                                        @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productReviewService.createReview(request, images));
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
