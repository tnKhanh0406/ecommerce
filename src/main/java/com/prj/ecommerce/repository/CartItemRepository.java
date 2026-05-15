package com.prj.ecommerce.repository;

import com.prj.ecommerce.dto.response.cart.CartItemSummaryResponse;
import com.prj.ecommerce.dto.response.cart.HeaderCartItemResponse;
import com.prj.ecommerce.entity.CartItemEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    List<CartItemEntity> findAllByCart_User_Id(Long userId);
    List<CartItemEntity> findAllByIdInAndCart_User_Id(List<Long> ids, Long userId);
    CartItemEntity findByCart_IdAndProductVariant_Id(Long cartId, Long productVariantId);
    @Query("""
        SELECT CASE WHEN COUNT(ci) > 0 THEN true ELSE false END
        FROM CartItemEntity ci
        WHERE ci.id = :cartItemId
          AND ci.cart.user.id = :userId
    """)
    boolean existsByIdAndUser(Long cartItemId, Long userId);
    long countByCartUserId(Long userId);
    @Query("""
        SELECT new com.prj.ecommerce.dto.response.cart.HeaderCartItemResponse(
            ci.id,
            pv.id,
            p.name,
            MIN(img.imageUrl),
            pv.price,
            ci.quantity
        )
        FROM CartItemEntity ci
        JOIN ci.productVariant pv
        JOIN pv.product p
        LEFT JOIN ProductImageEntity img
               ON img.variant.id = pv.id
        WHERE ci.cart.user.id = :userId
        GROUP BY
            ci.id,
            pv.id,
            p.name,
            pv.price,
            ci.quantity
        ORDER BY ci.id DESC
    """)
    List<HeaderCartItemResponse> getTop5HeaderCartItems(
            @Param("userId") Long userId,
            Pageable pageable
    );
    @Query("""
        SELECT new com.prj.ecommerce.dto.response.cart.CartItemSummaryResponse(
            ci.id,
            pv.id,
            p.name,
            pv.primaryImg,
            pv.price,
            ci.quantity,
            pv.stock,
            s.id,
            s.shopName
        )
        FROM CartItemEntity ci
        JOIN ci.productVariant pv
        JOIN pv.product p
        JOIN p.shop s
        WHERE ci.cart.user.id = :userId
    """)
    List<CartItemSummaryResponse> findCartItemSummaries(
            @Param("userId") Long userId
    );
    @Query("""
        SELECT new com.prj.ecommerce.dto.response.cart.CartItemSummaryResponse(
            ci.id,
            pv.id,
            p.name,
            pv.primaryImg,
            pv.price,
            ci.quantity,
            pv.stock,
            s.id,
            s.shopName
        )
        FROM CartItemEntity ci
        JOIN ci.productVariant pv
        JOIN pv.product p
        JOIN p.shop s
        WHERE ci.id = :cartItemId
    """)
    Optional<CartItemSummaryResponse> findCartItemSummaryById(
            @Param("cartItemId") Long cartItemId
    );
}
