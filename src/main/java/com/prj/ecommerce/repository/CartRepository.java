package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<CartEntity, Long> {
    CartEntity findByUser_Id(Long userId);
}
