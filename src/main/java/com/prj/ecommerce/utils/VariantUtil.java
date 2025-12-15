package com.prj.ecommerce.utils;

import com.prj.ecommerce.entity.ProductVariantAttributeValueEntity;
import com.prj.ecommerce.entity.ProductVariantEntity;

public class VariantUtil {
    public static String generateVariantName(ProductVariantEntity productVariantEntity) {
        StringBuilder sb = new StringBuilder();
        for (ProductVariantAttributeValueEntity avv : productVariantEntity.getAttributes()) {
            sb.append(avv.getAttributeValue().getProductAttribute().getName()).append(": ").append(avv.getDisplayName()).append(" ");
        }
        return sb.toString().trim();
    }
}
