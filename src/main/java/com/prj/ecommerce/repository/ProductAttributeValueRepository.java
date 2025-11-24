package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.ProductAttributeValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValueEntity, Long> {
    Optional<ProductAttributeValueEntity> findByProductAttribute_IdAndValue(Long id, String value);
}
