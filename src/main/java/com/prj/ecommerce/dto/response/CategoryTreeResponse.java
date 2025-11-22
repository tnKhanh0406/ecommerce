package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.entity.CategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeResponse {
    private Long id;
    private String name;
    private String slug;
    private List<CategoryTreeResponse> children = new ArrayList<>();

    public static CategoryTreeResponse fromEntity(CategoryEntity category) {
        CategoryTreeResponse response = new CategoryTreeResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            response.setChildren(
                    category.getChildren()
                            .stream()
                            .map(CategoryTreeResponse::fromEntity)
                            .toList()
            );
        }

        return response;
    }
}

