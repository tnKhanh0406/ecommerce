package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.entity.CategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private Long parentId;

    public static CategoryResponse fromEntity(CategoryEntity category) {
        return new CategoryResponse(
          category.getId(),
          category.getName(),
          category.getSlug(),
          category.getParent() != null ? category.getParent().getId() : null
        );
    }
}
