package com.prj.ecommerce.dto.response.category;

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
}

