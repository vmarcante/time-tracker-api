package com.vmarcante.time_tracker.core.infrastructure.company.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyFilter;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class CompanyRepositorySpecification {

    private CompanyRepositorySpecification() {
    }

    public static Specification<CompanyJpaEntity> withFilter(CompanyFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                hasId(filter.getId()),
                hasIdIn(filter.getIds()),
                hasDocument(filter.getDocument()),
                isActive(filter.getActive()),
                hasActiveApprovedMember(filter.getMemberUserId())));
    }

    public static Specification<CompanyJpaEntity> hasId(UUID id) {
        return (root, query, cb) -> id == null
                ? cb.conjunction()
                : cb.equal(root.get("id"), id);
    }

    public static Specification<CompanyJpaEntity> hasIdIn(Collection<UUID> ids) {
        return (root, query, cb) -> ids == null || ids.isEmpty()
                ? cb.conjunction()
                : root.get("id").in(ids);
    }

    public static Specification<CompanyJpaEntity> hasDocument(String document) {
        return (root, query, cb) -> document == null || document.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("document"), document);
    }

    public static Specification<CompanyJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }

    public static Specification<CompanyJpaEntity> hasActiveApprovedMember(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return cb.conjunction();
            }
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<CompanyMembershipJpaEntity> membership = subquery.from(CompanyMembershipJpaEntity.class);
            subquery.select(membership.get("companyId"))
                    .where(cb.equal(membership.get("userId"), userId),
                            cb.isTrue(membership.get("active")),
                            cb.isTrue(membership.get("approved")));
            return root.get("id").in(subquery);
        };
    }
}
