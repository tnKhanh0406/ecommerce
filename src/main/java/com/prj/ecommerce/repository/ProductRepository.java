package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.ProductEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {
    @Query("""
        SELECT p
        FROM ProductEntity p
        WHERE p.status = 'ACTIVE'
        ORDER BY function('RAND')
    """)
    List<ProductEntity> findRandomProducts(Pageable pageable);

    @Query("""
        SELECT pc.category.id
        FROM ProductCategoryEntity pc
        WHERE pc.product.id = :productId
        ORDER BY pc.category.id ASC
    """)
    List<Long> getCategoryIdsByProductId(@Param("productId") Long productId);

    long countByShop_Id(Long shopId);
}
