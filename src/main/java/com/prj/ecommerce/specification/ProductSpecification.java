package com.prj.ecommerce.specification;

import com.prj.ecommerce.common.ProductSortType;
import com.prj.ecommerce.dto.request.ProductFilterRequest;
import com.prj.ecommerce.entity.CategoryEntity;
import com.prj.ecommerce.entity.ProductCategoryEntity;
import com.prj.ecommerce.entity.ProductEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<ProductEntity> search(ProductFilterRequest req, List<Long> categoryIds) {
        return (root, query, cb) -> {

            Predicate predicate = filter(
                    req.getKeyword(),
                    categoryIds,
                    req.getShopId(),
                    req.getMinPrice(),
                    req.getMaxPrice()
            ).toPredicate(root, query, cb);

            applySort(root, query, cb, req.getSortType());

            return predicate;
        };
    }


    public static Specification<ProductEntity> filter (String keyword,
                                                       List<Long> categoryIds,
                                                       Long shopId,
                                                       BigDecimal minPrice,
                                                       BigDecimal maxPrice) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            /* keyword */
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(
                        cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%")
                );
            }

            /* category (M:N) */
            if (categoryIds != null && !categoryIds.isEmpty()) {
                Join<ProductEntity, ProductCategoryEntity> pcJoin =
                        root.join("productCategories", JoinType.INNER);

                Join<ProductCategoryEntity, CategoryEntity> categoryJoin =
                        pcJoin.join("category", JoinType.INNER);

                predicates.add(categoryJoin.get("id").in(categoryIds));
            }

            /* shop */
            if (shopId != null) {
                predicates.add(
                        cb.equal(root.get("shop").get("id"), shopId)
                );
            }

            /* price nằm ở product_variant */
            if (minPrice != null || maxPrice != null) {
                Join<Object, Object> variantJoin = root.join("variants", JoinType.INNER);

                if (minPrice != null) {
                    predicates.add(cb.greaterThanOrEqualTo(variantJoin.get("price"), minPrice));
                }

                if (maxPrice != null) {
                    predicates.add(cb.lessThanOrEqualTo(variantJoin.get("price"), maxPrice));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static void applySort(Root<ProductEntity> root,
                                 CriteriaQuery<?> query,
                                 CriteriaBuilder cb,
                                 ProductSortType sortType) {
        if (sortType == null) {
            query.orderBy(cb.desc(root.get("createdAt")));
            return;
        }

        switch (sortType) {
            case PRICE_ASC -> {
                Join<Object, Object> v = root.join("variants", JoinType.LEFT);
                query.groupBy(root.get("id"));
                query.orderBy(cb.asc(cb.min(v.get("price"))));
            }

            case PRICE_DESC -> {
                Join<Object, Object> v = root.join("variants", JoinType.LEFT);
                query.groupBy(root.get("id"));
                query.orderBy(cb.desc(cb.min(v.get("price"))));
            }

            case SOLD_DESC ->
                    query.orderBy(cb.desc(root.get("soldCount")));

            case NEWEST ->
                    query.orderBy(cb.desc(root.get("createdAt")));

            case RATING_DESC ->
                    query.orderBy(cb.desc(root.get("rating")));
        }
    }

}
