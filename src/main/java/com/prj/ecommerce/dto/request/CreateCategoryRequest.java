package com.prj.ecommerce.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {
    private String name;
    private String slug;
    private Long parentId;
}
