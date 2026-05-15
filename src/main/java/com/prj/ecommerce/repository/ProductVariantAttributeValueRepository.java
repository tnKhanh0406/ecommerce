package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.ProductVariantAttributeValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductVariantAttributeValueRepository extends JpaRepository<ProductVariantAttributeValueEntity, Long> {

    @Query(
            value = """
        SELECT vav.variant_id
        FROM variant_attribute_values vav
        JOIN product_variants v ON v.id = vav.variant_id
        WHERE v.product_id = :productId
          AND vav.attribute_value_id IN (:valueIds)
        GROUP BY vav.variant_id
        HAVING COUNT(*) = :size
        LIMIT 1
    """,
            nativeQuery = true
    )
    Long findProductVariantIdByProductAndVariantAttributeValues(
            @Param("productId") Long productId,
            @Param("valueIds") List<Long> valueIds,
            @Param("size") int size
    );

    @Query("""
        SELECT vav
        FROM ProductVariantAttributeValueEntity vav
        JOIN vav.variant v
        WHERE v.product.id = :productId
    """)
    List<ProductVariantAttributeValueEntity> findProductVariantAttributeValues(@Param("productId") Long productId);

    @Query("""
        SELECT
            pv.id,
            pa.name,
            av.value
        FROM ProductVariantAttributeValueEntity pav
        JOIN pav.variant pv
        JOIN pav.attributeValue av
        JOIN av.productAttribute pa
        WHERE pv.id IN :variantIds
    """)
    List<Object[]> findVariantAttributes(
            @Param("variantIds") List<Long> variantIds
    );

    @Query("""
        SELECT
            pa.name,
            av.value
        FROM ProductVariantAttributeValueEntity pav
        JOIN pav.attributeValue av
        JOIN av.productAttribute pa
        WHERE pav.variant.id = :variantId
    """)
    List<Object[]> findVariantAttributesByVariantId(
            @Param("variantId") Long variantId
    );
}
