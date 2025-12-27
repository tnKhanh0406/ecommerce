package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.ProductReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReviewEntity, Long> {
    List<ProductReviewEntity> findByProduct_Id(Long productId);
    boolean existsByOrderItem_Id(Long id);
}
