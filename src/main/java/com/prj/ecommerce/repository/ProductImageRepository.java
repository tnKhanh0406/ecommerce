package com.prj.ecommerce.repository;

import com.prj.ecommerce.common.ImageType;
import com.prj.ecommerce.entity.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {
    List<ProductImageEntity> findAllByProduct_IdAndImageType(Long product_id, ImageType imageType);
}
