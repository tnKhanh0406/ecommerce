package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddressEntity, Long> {
    Optional<UserAddressEntity> findByUser_IdAndIsDefault(Long userId, Integer isDefault);
}
