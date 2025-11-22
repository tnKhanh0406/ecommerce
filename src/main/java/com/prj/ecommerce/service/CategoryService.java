package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.response.CategoryResponse;
import com.prj.ecommerce.dto.response.CategoryTreeResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getTopLevelCategories();
    List<CategoryTreeResponse> getCategoriesTree();
}
