package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.VoucherEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<VoucherEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VoucherEntity v WHERE v.id = :id")
    Optional<VoucherEntity> findByIdForUpdate(Long id);
    List<VoucherEntity> findAllByShopId(Long shopId);
}
