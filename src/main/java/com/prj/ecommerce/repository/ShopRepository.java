package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.ShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<ShopEntity, Long> {
    Optional<ShopEntity> findByUser_Id(Long id);
}
