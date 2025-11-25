package com.prj.ecommerce.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantAttributeValueRequest {
    private String attribute;
    private String value;
    private String displayName;
}
