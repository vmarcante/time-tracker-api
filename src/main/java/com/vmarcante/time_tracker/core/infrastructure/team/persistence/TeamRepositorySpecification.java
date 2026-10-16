package com.vmarcante.time_tracker.core.infrastructure.team.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilter;
import com.vmarcante.time_tracker.core.infrastructure.project.persistence.ProjectTeamJpaEntity;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class TeamRepositorySpecification {

    private TeamRepositorySpecification() {
    }

    public static Specification<TeamJpaEntity> withFilter(TeamFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                hasId(filter.getId()),
                hasIdIn(filter.getIds()),
                hasCompanyId(filter.getCompanyId()),
                hasNameIgnoreCase(filter.getName()),
                isActive(filter.getActive()),
                hasActiveApprovedMember(filter.getMemberUserId()),
                hasActiveProjectLink(filter.getLinkedProjectId())));
    }

    public static Specification<TeamJpaEntity> hasId(UUID id) {
        return (root, query, cb) -> id == null
                ? cb.conjunction()
                : cb.equal(root.get("id"), id);
    }

    public static Specification<TeamJpaEntity> hasIdIn(Collection<UUID> ids) {
        return (root, query, cb) -> ids == null || ids.isEmpty()
                ? cb.conjunction()
                : root.get("id").in(ids);
    }

    public static Specification<TeamJpaEntity> hasCompanyId(UUID companyId) {
        return (root, query, cb) -> companyId == null
                ? cb.conjunction()
                : cb.equal(root.get("companyId"), companyId);
    }

    public static Specification<TeamJpaEntity> hasNameIgnoreCase(String name) {
        return (root, query, cb) -> name == null || name.isBlank()
                ? cb.conjunction()
                : cb.equal(cb.lower(root.get("name")), name.toLowerCase());
    }

    public static Specification<TeamJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }

    public static Specification<TeamJpaEntity> hasActiveApprovedMember(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return cb.conjunction();
            }
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<TeamMembershipJpaEntity> membership = subquery.from(TeamMembershipJpaEntity.class);
            subquery.select(membership.get("teamId"))
                    .where(cb.equal(membership.get("userId"), userId),
                            cb.isTrue(membership.get("active")),
                            cb.isTrue(membership.get("approved")));
            return root.get("id").in(subquery);
        };
    }

    public static Specification<TeamJpaEntity> hasActiveProjectLink(UUID projectId) {
        return (root, query, cb) -> {
            if (projectId == null) {
                return cb.conjunction();
            }
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<ProjectTeamJpaEntity> link = subquery.from(ProjectTeamJpaEntity.class);
            subquery.select(link.get("teamId"))
                    .where(cb.equal(link.get("projectId"), projectId),
                            cb.isTrue(link.get("active")));
            return root.get("id").in(subquery);
        };
    }
}
