package com.prj.ecommerce.repository;

import com.prj.ecommerce.common.Status;
import com.prj.ecommerce.entity.ShopEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<ShopEntity, Long> {
    Optional<ShopEntity> findByUser_Id(Long id);
    
    Page<ShopEntity> findByStatus(Status status, Pageable pageable);
    
    @Query("SELECT s FROM ShopEntity s WHERE " +
            "LOWER(s.shopName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.user.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<ShopEntity> searchShops(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT s FROM ShopEntity s WHERE s.status = :status AND " +
            "(LOWER(s.shopName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.user.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ShopEntity> searchShopsByStatus(@Param("status") Status status, @Param("search") String search, Pageable pageable);

    boolean existsByIdAndUser_Id(Long id, Long userId);
}
