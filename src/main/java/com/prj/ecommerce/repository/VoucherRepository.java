package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.VoucherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoucherRepository extends JpaRepository<VoucherEntity, Long> {
    List<VoucherEntity> findAllByShopId(Long shopId);
}
