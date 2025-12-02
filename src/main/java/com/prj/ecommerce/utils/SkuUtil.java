package com.prj.ecommerce.utils;

import com.prj.ecommerce.entity.ProductEntity;

public class SkuUtil {

    public static String generateSku(ProductEntity product, int i) {
        StringBuilder sb = new StringBuilder();
        String[] strings = product.getName().split(" ");
        for (String s : strings) {
            sb.append(s.substring(0, 1).toUpperCase());
        }
        long time = System.currentTimeMillis() % 10000 + i;
        sb.append("-").append(time);
        return sb.toString();
    }
}
