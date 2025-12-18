package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.VoucherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<VoucherEntity, Long> {
}
