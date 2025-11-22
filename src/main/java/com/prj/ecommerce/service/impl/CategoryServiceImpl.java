package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.dto.response.CategoryResponse;
import com.prj.ecommerce.dto.response.CategoryTreeResponse;
import com.prj.ecommerce.repository.CategoryRepository;
import com.prj.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getTopLevelCategories() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    @Override
    public List<CategoryTreeResponse> getCategoriesTree() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryTreeResponse::fromEntity)
                .toList();
    }
}
