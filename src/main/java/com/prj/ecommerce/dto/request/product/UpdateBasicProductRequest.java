package com.prj.ecommerce.dto.request.product;

import com.prj.ecommerce.dto.request.image.ProductImageRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UpdateBasicProductRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    private List<Long> categoryIds;
    private List<ProductImageRequest> images;
}
