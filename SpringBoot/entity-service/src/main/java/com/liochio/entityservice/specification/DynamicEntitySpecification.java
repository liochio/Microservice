package com.liochio.entityservice.specification;

import com.liochio.entityservice.entity.DynamicEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * ==============================================================================
 * Tiêu Chí Tìm Kiếm Thực Thể Động (Dynamic Entity Specification)
 * ==============================================================================
 */
public class DynamicEntitySpecification {

    public static Specification<DynamicEntity> filter(String entityType, String status, String keyword, String tenantId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tenantId != null && !tenantId.isBlank()) {
                predicates.add(cb.equal(root.get("tenantId"), tenantId));
            }

            if (entityType != null && !entityType.isBlank()) {
                predicates.add(cb.equal(root.get("entityType"), entityType.trim()));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }

            if (keyword != null && !keyword.isBlank()) {
                String likePattern = "%" + keyword.toLowerCase().trim() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), likePattern);
                Predicate slugLike = cb.like(cb.lower(root.get("slug")), likePattern);
                predicates.add(cb.or(titleLike, slugLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
