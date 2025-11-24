package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.ProductAttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductAttributeRepository extends JpaRepository<ProductAttributeEntity, Long> {
    Optional<ProductAttributeEntity> findByNameAndSeller_Id(String name, Long sellerId);
    Optional<ProductAttributeEntity> findByNameAndSellerIsNull(String name);
    // helper to find global OR seller-specific
    default Optional<ProductAttributeEntity> findByNameForSeller(String name, Long sellerId) {
        if (sellerId != null) {
            Optional<ProductAttributeEntity> r = findByNameAndSeller_Id(name, sellerId);
            if (r.isPresent()) return r;
        }
        return findByNameAndSellerIsNull(name);
    }
}
