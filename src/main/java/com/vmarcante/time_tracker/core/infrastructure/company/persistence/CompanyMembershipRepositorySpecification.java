package com.vmarcante.time_tracker.core.infrastructure.company.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

public final class CompanyMembershipRepositorySpecification {

    private CompanyMembershipRepositorySpecification() {
    }

    public static Specification<CompanyMembershipJpaEntity> withFilter(CompanyMembershipFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                hasId(filter.getId()),
                hasUserId(filter.getUserId()),
                hasCompanyId(filter.getCompanyId()),
                hasRole(filter.getRole()),
                hasOrigin(filter.getOrigin()),
                isApproved(filter.getApproved()),
                isActive(filter.getActive()),
                isResolved(filter.getResolved())));
    }

    public static Specification<CompanyMembershipJpaEntity> hasId(UUID id) {
        return (root, query, cb) -> id == null
                ? cb.conjunction()
                : cb.equal(root.get("id"), id);
    }

    public static Specification<CompanyMembershipJpaEntity> hasUserId(UUID userId) {
        return (root, query, cb) -> userId == null
                ? cb.conjunction()
                : cb.equal(root.get("userId"), userId);
    }

    public static Specification<CompanyMembershipJpaEntity> hasCompanyId(UUID companyId) {
        return (root, query, cb) -> companyId == null
                ? cb.conjunction()
                : cb.equal(root.get("companyId"), companyId);
    }

    public static Specification<CompanyMembershipJpaEntity> hasRole(CompanyRole role) {
        return (root, query, cb) -> role == null
                ? cb.conjunction()
                : cb.equal(root.get("role"), role);
    }

    public static Specification<CompanyMembershipJpaEntity> hasOrigin(MembershipOrigin origin) {
        return (root, query, cb) -> origin == null
                ? cb.conjunction()
                : cb.equal(root.get("origin"), origin);
    }

    public static Specification<CompanyMembershipJpaEntity> isApproved(Boolean approved) {
        return (root, query, cb) -> approved == null
                ? cb.conjunction()
                : cb.equal(root.get("approved"), approved);
    }

    public static Specification<CompanyMembershipJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }

    public static Specification<CompanyMembershipJpaEntity> isResolved(Boolean resolved) {
        return (root, query, cb) -> {
            if (resolved == null) {
                return cb.conjunction();
            }
            return resolved
                    ? cb.or(cb.isTrue(root.get("approved")), cb.isFalse(root.get("active")))
                    : cb.and(cb.isFalse(root.get("approved")), cb.isTrue(root.get("active")));
        };
    }
}
