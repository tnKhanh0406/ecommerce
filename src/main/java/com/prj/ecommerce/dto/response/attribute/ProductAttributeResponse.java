package com.prj.ecommerce.dto.response.attribute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeResponse {
    private Long id;
    private String name;
    private List<ProductAttributeValueResponse> values;
}
