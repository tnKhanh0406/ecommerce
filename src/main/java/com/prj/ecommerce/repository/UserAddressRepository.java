package com.prj.ecommerce.repository;

import com.prj.ecommerce.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddressEntity, Long> {
}
