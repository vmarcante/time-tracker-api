package com.vmarcante.time_tracker.core.infrastructure.team.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.team.enums.TeamRole;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamMembershipFilter;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class TeamMembershipRepositorySpecification {

    private TeamMembershipRepositorySpecification() {
    }

    public static Specification<TeamMembershipJpaEntity> withFilter(TeamMembershipFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                hasId(filter.getId()),
                hasUserId(filter.getUserId()),
                hasTeamId(filter.getTeamId()),
                hasTeamIdIn(filter.getTeamIds()),
                hasCompanyId(filter.getCompanyId()),
                hasRole(filter.getRole()),
                isApproved(filter.getApproved()),
                isActive(filter.getActive())));
    }

    public static Specification<TeamMembershipJpaEntity> hasId(UUID id) {
        return (root, query, cb) -> id == null
                ? cb.conjunction()
                : cb.equal(root.get("id"), id);
    }

    public static Specification<TeamMembershipJpaEntity> hasUserId(UUID userId) {
        return (root, query, cb) -> userId == null
                ? cb.conjunction()
                : cb.equal(root.get("userId"), userId);
    }

    public static Specification<TeamMembershipJpaEntity> hasTeamId(UUID teamId) {
        return (root, query, cb) -> teamId == null
                ? cb.conjunction()
                : cb.equal(root.get("teamId"), teamId);
    }

    public static Specification<TeamMembershipJpaEntity> hasTeamIdIn(Collection<UUID> teamIds) {
        return (root, query, cb) -> teamIds == null || teamIds.isEmpty()
                ? cb.conjunction()
                : root.get("teamId").in(teamIds);
    }

    public static Specification<TeamMembershipJpaEntity> hasCompanyId(UUID companyId) {
        return (root, query, cb) -> {
            if (companyId == null) {
                return cb.conjunction();
            }
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<TeamJpaEntity> team = subquery.from(TeamJpaEntity.class);
            subquery.select(team.get("id"))
                    .where(cb.equal(team.get("companyId"), companyId));
            return root.get("teamId").in(subquery);
        };
    }

    public static Specification<TeamMembershipJpaEntity> hasRole(TeamRole role) {
        return (root, query, cb) -> role == null
                ? cb.conjunction()
                : cb.equal(root.get("role"), role);
    }

    public static Specification<TeamMembershipJpaEntity> isApproved(Boolean approved) {
        return (root, query, cb) -> approved == null
                ? cb.conjunction()
                : cb.equal(root.get("approved"), approved);
    }

    public static Specification<TeamMembershipJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }
}
