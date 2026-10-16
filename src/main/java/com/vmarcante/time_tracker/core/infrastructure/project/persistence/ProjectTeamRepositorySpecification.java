package com.vmarcante.time_tracker.core.infrastructure.project.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.project.model.filter.TeamProjectFilter;
import com.vmarcante.time_tracker.core.infrastructure.team.persistence.TeamJpaEntity;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class ProjectTeamRepositorySpecification {

    private ProjectTeamRepositorySpecification() {
    }

    public static Specification<ProjectTeamJpaEntity> withFilter(TeamProjectFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                hasId(filter.getId()),
                hasProjectId(filter.getProjectId()),
                hasTeamId(filter.getTeamId()),
                isActive(filter.getActive()),
                hasTeamActive(filter.getTeamActive()),
                hasProjectActive(filter.getProjectActive())));
    }

    public static Specification<ProjectTeamJpaEntity> hasId(UUID id) {
        return (root, query, cb) -> id == null
                ? cb.conjunction()
                : cb.equal(root.get("id"), id);
    }

    public static Specification<ProjectTeamJpaEntity> hasProjectId(UUID projectId) {
        return (root, query, cb) -> projectId == null
                ? cb.conjunction()
                : cb.equal(root.get("projectId"), projectId);
    }

    public static Specification<ProjectTeamJpaEntity> hasTeamId(UUID teamId) {
        return (root, query, cb) -> teamId == null
                ? cb.conjunction()
                : cb.equal(root.get("teamId"), teamId);
    }

    public static Specification<ProjectTeamJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }

    public static Specification<ProjectTeamJpaEntity> hasTeamActive(Boolean teamActive) {
        return (root, query, cb) -> {
            if (teamActive == null) {
                return cb.conjunction();
            }
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<TeamJpaEntity> team = subquery.from(TeamJpaEntity.class);
            subquery.select(team.get("id"))
                    .where(cb.equal(team.get("active"), teamActive));
            return root.get("teamId").in(subquery);
        };
    }

    public static Specification<ProjectTeamJpaEntity> hasProjectActive(Boolean projectActive) {
        return (root, query, cb) -> {
            if (projectActive == null) {
                return cb.conjunction();
            }
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<ProjectJpaEntity> project = subquery.from(ProjectJpaEntity.class);
            subquery.select(project.get("id"))
                    .where(cb.equal(project.get("active"), projectActive));
            return root.get("projectId").in(subquery);
        };
    }
}
