package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.ProductVariantAttributeValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantAttributeValueRepository extends JpaRepository<ProductVariantAttributeValueEntity, Long> {
}
