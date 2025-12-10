package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    List<CartItemEntity> findAllByCart_User_Id(Long userId);
    CartItemEntity findByCart_IdAndProductVariant_Id(Long cartId, Long productVariantId);
}
