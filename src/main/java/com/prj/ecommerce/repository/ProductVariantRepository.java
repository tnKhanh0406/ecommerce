package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.ProductVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, Long> {
}
