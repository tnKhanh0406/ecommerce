package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.ProductReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductReviewRepository extends JpaRepository<ProductReviewEntity, Long> {
    boolean existsByOrderItem_Id(Long id);
}
