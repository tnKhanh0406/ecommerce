package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.ProductCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategoryEntity, Long> {
    List<ProductCategoryEntity> findAllByProduct_Id(Long productId);
    void deleteAllByProduct_Id(Long productId);
}
