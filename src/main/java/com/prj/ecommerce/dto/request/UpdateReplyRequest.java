package com.prj.ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateReplyRequest {
    @NotNull
    @Size(max = 1000)
    private String content;
}
