package com.vmarcante.time_tracker.core.infrastructure.project.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilter;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class ProjectAssignmentRepositorySpecification {

    private ProjectAssignmentRepositorySpecification() {
    }

    public static Specification<ProjectAssignmentJpaEntity> withFilter(ProjectAssignmentFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                hasId(filter.getId()),
                hasIdIn(filter.getIds()),
                hasUserId(filter.getUserId()),
                hasProjectId(filter.getProjectId()),
                hasTeamId(filter.getTeamId()),
                hasProjectCompanyId(filter.getCompanyId()),
                isActive(filter.getActive())));
    }

    public static Specification<ProjectAssignmentJpaEntity> hasId(UUID id) {
        return (root, query, cb) -> id == null
                ? cb.conjunction()
                : cb.equal(root.get("id"), id);
    }

    public static Specification<ProjectAssignmentJpaEntity> hasIdIn(Collection<UUID> ids) {
        return (root, query, cb) -> ids == null || ids.isEmpty()
                ? cb.conjunction()
                : root.get("id").in(ids);
    }

    public static Specification<ProjectAssignmentJpaEntity> hasUserId(UUID userId) {
        return (root, query, cb) -> userId == null
                ? cb.conjunction()
                : cb.equal(root.get("userId"), userId);
    }

    public static Specification<ProjectAssignmentJpaEntity> hasProjectId(UUID projectId) {
        return (root, query, cb) -> projectId == null
                ? cb.conjunction()
                : cb.equal(root.get("projectId"), projectId);
    }

    public static Specification<ProjectAssignmentJpaEntity> hasTeamId(UUID teamId) {
        return (root, query, cb) -> teamId == null
                ? cb.conjunction()
                : cb.equal(root.get("teamId"), teamId);
    }

    public static Specification<ProjectAssignmentJpaEntity> hasProjectCompanyId(UUID companyId) {
        return (root, query, cb) -> {
            if (companyId == null) {
                return cb.conjunction();
            }
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<ProjectJpaEntity> project = subquery.from(ProjectJpaEntity.class);
            subquery.select(project.get("id"))
                    .where(cb.equal(project.get("companyId"), companyId));
            return root.get("projectId").in(subquery);
        };
    }

    public static Specification<ProjectAssignmentJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }
}
