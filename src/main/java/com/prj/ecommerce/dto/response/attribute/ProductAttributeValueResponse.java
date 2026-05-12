package com.prj.ecommerce.dto.response.attribute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeValueResponse {
    private Long id;
    private String value;
    private String displayName;
}
