package com.prj.ecommerce.repository;

import com.prj.ecommerce.dto.response.category.CategoryResponse;
import com.prj.ecommerce.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByParentIsNull();
    List<CategoryEntity> findByParentId(Long id);
    CategoryEntity findBySlug(String slug);
    @Query("""
    SELECT new com.prj.ecommerce.dto.response.category.CategoryResponse(
      c.id, c.name, c.imageUrl, c.slug, p.id
    )
    FROM CategoryEntity c
    LEFT JOIN c.parent p
    """)
    List<CategoryResponse> findAllCategoryResponse();
    @Query("""
    SELECT new com.prj.ecommerce.dto.response.category.CategoryResponse(
      c.id, c.name, c.imageUrl, c.slug, p.id
    )
    FROM CategoryEntity c
    LEFT JOIN c.parent p
    WHERE c.id =:categoryId
    """)
    CategoryResponse findCategoryResponseById(Long categoryId);
    @Query("""
        SELECT DISTINCT pc.category.id
        FROM ProductCategoryEntity pc
        WHERE pc.product.shop.id = :shopId
        ORDER BY pc.category.id ASC
    """)
    List<Long> getCategoryIdsByShopId(@Param("shopId") Long shopId);
}
