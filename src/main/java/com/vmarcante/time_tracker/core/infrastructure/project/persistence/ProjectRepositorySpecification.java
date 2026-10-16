package com.vmarcante.time_tracker.core.infrastructure.project.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectFilter;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class ProjectRepositorySpecification {

    private ProjectRepositorySpecification() {
    }

    public static Specification<ProjectJpaEntity> withFilter(ProjectFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                hasId(filter.getId()),
                hasIdIn(filter.getIds()),
                hasCompanyId(filter.getCompanyId()),
                hasUserId(filter.getUserId()),
                hasNameIgnoreCase(filter.getName()),
                hasStatus(filter.getStatus()),
                isActive(filter.getActive()),
                hasActiveAssignee(filter.getAssigneeUserId()),
                hasActiveTeamLink(filter.getLinkedTeamId())));
    }

    public static Specification<ProjectJpaEntity> hasId(UUID id) {
        return (root, query, cb) -> id == null
                ? cb.conjunction()
                : cb.equal(root.get("id"), id);
    }

    public static Specification<ProjectJpaEntity> hasIdIn(Collection<UUID> ids) {
        return (root, query, cb) -> ids == null || ids.isEmpty()
                ? cb.conjunction()
                : root.get("id").in(ids);
    }

    public static Specification<ProjectJpaEntity> hasCompanyId(UUID companyId) {
        return (root, query, cb) -> companyId == null
                ? cb.conjunction()
                : cb.equal(root.get("companyId"), companyId);
    }

    public static Specification<ProjectJpaEntity> hasUserId(UUID userId) {
        return (root, query, cb) -> userId == null
                ? cb.conjunction()
                : cb.equal(root.get("userId"), userId);
    }

    public static Specification<ProjectJpaEntity> hasNameIgnoreCase(String name) {
        return (root, query, cb) -> name == null || name.isBlank()
                ? cb.conjunction()
                : cb.equal(cb.lower(root.get("name")), name.toLowerCase());
    }

    public static Specification<ProjectJpaEntity> hasStatus(ProjectStatus status) {
        return (root, query, cb) -> status == null
                ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    public static Specification<ProjectJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }

    public static Specification<ProjectJpaEntity> hasActiveAssignee(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return cb.conjunction();
            }
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<ProjectAssignmentJpaEntity> assignment = subquery.from(ProjectAssignmentJpaEntity.class);
            subquery.select(assignment.get("projectId"))
                    .where(cb.equal(assignment.get("userId"), userId),
                            cb.isTrue(assignment.get("active")));
            return root.get("id").in(subquery);
        };
    }

    public static Specification<ProjectJpaEntity> hasActiveTeamLink(UUID teamId) {
        return (root, query, cb) -> {
            if (teamId == null) {
                return cb.conjunction();
            }
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<ProjectTeamJpaEntity> link = subquery.from(ProjectTeamJpaEntity.class);
            subquery.select(link.get("projectId"))
                    .where(cb.equal(link.get("teamId"), teamId),
                            cb.isTrue(link.get("active")));
            return root.get("id").in(subquery);
        };
    }
}
