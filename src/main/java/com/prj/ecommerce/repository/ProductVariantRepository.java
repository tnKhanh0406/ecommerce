package com.prj.ecommerce.repository;

import com.prj.ecommerce.dto.response.product.ProductPriceRangeResponse;
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
    @Query("""
        SELECT new com.prj.ecommerce.dto.response.ProductPriceRangeResponse(
            p.id,
            MIN(v.price),
            MAX(v.price)
        )
        FROM ProductEntity p
        JOIN p.variants v
        WHERE p.id IN :productIds
        GROUP BY p.id
    """)
    List<ProductPriceRangeResponse> findPriceRangeByProductIds(
            @Param("productIds") List<Long> productIds
    );

    @Query("""
    SELECT new com.prj.ecommerce.dto.response.ProductPriceRangeResponse(
            pv.product.id,
            MIN(pv.price),
            MAX(pv.price)
        )
        FROM ProductVariantEntity pv
        WHERE pv.product.id = :productId
        GROUP BY pv.product.id
    """)
    Optional<ProductPriceRangeResponse> findPriceRangeByProductId(@Param("productId") Long productId);
}
