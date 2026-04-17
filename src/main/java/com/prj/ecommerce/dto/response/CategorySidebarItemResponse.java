package com.prj.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySidebarItemResponse {
    private Long id;
    private String name;
    private String slug;
    private int level;
}