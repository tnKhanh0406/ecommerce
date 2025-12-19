package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.ProductVariantEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pv FROM ProductVariantEntity pv WHERE pv.id = :id")
    Optional<ProductVariantEntity> findByIdForUpdate(@Param("id") Long id);
    List<ProductVariantEntity> findByProductId(Long productId);
}
