package com.prj.ecommerce.repository;

import com.prj.ecommerce.common.ReferenceType;
import com.prj.ecommerce.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

//    @Query("""
//    SELECT n FROM NotificationEntity n
//    WHERE n.user.id = :userId
//      AND (:referenceType IS NULL OR n.referenceType = :referenceType)
//      AND (:isRead IS NULL OR n.isRead = :isRead)
//    ORDER BY n.createdAt DESC
//""")
//    List<NotificationEntity> findNotifications(
//            @Param("userId") Long userId,
//            @Param("referenceType") ReferenceType referenceType,
//            @Param("isRead") Boolean isRead
//    );

    List<NotificationEntity> findTop5ByUser_IdOrderByCreatedAtDesc(Long userId);
    List<NotificationEntity> findAllByUser_IdAndReferenceType(Long userId, ReferenceType referenceType);
}
