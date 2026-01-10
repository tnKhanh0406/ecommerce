package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByParentIsNull();
    List<CategoryEntity> findByParentId(Long id);
}
