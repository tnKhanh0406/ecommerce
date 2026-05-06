package com.prj.ecommerce.dto.request.review;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewReplyRequest {
    @Size(max = 1000)
    @NotNull
    private String content;

    @NotNull
    private Long reviewId;
}
