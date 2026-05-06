package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByParentIsNull();
    List<CategoryEntity> findByParentId(Long id);
    CategoryEntity findBySlug(String slug);
    @Query("SELECT c FROM CategoryEntity c")
    List<CategoryEntity> findAll();
}
