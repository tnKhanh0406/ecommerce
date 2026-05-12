package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    List<CartItemEntity> findTop5ByCart_User_IdOrderByIdDesc(Long userId);
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
}
