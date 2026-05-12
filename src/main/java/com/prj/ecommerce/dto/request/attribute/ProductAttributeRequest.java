package com.prj.ecommerce.dto.request.attribute;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductAttributeRequest {
    @NotBlank
    private String name;

    private List<ProductAttributeValueRequest> values;
}
