package com.prj.ecommerce.specification;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.entity.OrderEntity;
import com.prj.ecommerce.entity.UserEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {
    public static Specification<OrderEntity> filterAdminOrders(
            String keyword,
            OrderStatus status
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<OrderEntity, UserEntity> userJoin = root.join("user", JoinType.LEFT);

            // keyword
            if (keyword != null && !keyword.isBlank()) {

                String likeKeyword = "%" + keyword.toLowerCase().trim() + "%";

                Predicate keywordPredicate = cb.or(
                        cb.like(cb.lower(root.get("shopName")), likeKeyword),
                        cb.like(cb.lower(root.get("receiverName")), likeKeyword),
                        cb.like(cb.lower(userJoin.get("username")), likeKeyword),
                        cb.like(cb.lower(userJoin.get("email")), likeKeyword),
                        cb.like(root.get("id").as(String.class), "%" + keyword.trim() + "%")
                );

                predicates.add(keywordPredicate);
            }

            // status
            if (status != null) {
                predicates.add(cb.equal(root.get("orderStatus"), status));
            }

            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<OrderEntity> filterShopOrders(
            Long shopId,
            OrderStatus status
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    cb.equal(root.get("shop").get("id"), shopId)
            );

            if (status != null) {
                predicates.add(
                        cb.equal(root.get("orderStatus"), status)
                );
            }

            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
