package com.vmarcante.time_tracker.core.infrastructure.timeentry.persistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.team.enums.TeamRole;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntryFilter;
import com.vmarcante.time_tracker.core.infrastructure.project.persistence.ProjectTeamJpaEntity;
import com.vmarcante.time_tracker.core.infrastructure.team.persistence.TeamMembershipJpaEntity;

import org.hibernate.query.criteria.HibernateCriteriaBuilder;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class TimeEntryRepositorySpecification {

    private TimeEntryRepositorySpecification() {
    }

    public static Specification<TimeEntryJpaEntity> withFilter(TimeEntryFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                active(),
                hasId(filter.getId()),
                hasUserId(filter.getUserId()),
                hasCompanyId(filter.getCompanyId()),
                visibleToLead(filter.getLeadScopeUserId()),
                matchesEntityFilter(filter.getUserIds(), filter.getTeamIds()),
                hasProjectId(filter.getProjectId()),
                hasAnyTag(filter.getTagIds()),
                nameContains(filter.getName()),
                overlapsRange(filter.getFrom(), filter.getTo())));
    }

    public static Specification<TimeEntryJpaEntity> active() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<TimeEntryJpaEntity> hasId(UUID id) {
        return (root, query, cb) -> id == null
                ? cb.conjunction()
                : cb.equal(root.get("id"), id);
    }

    public static Specification<TimeEntryJpaEntity> hasUserId(UUID userId) {
        return (root, query, cb) -> userId == null
                ? cb.conjunction()
                : cb.equal(root.get("userId"), userId);
    }

    public static Specification<TimeEntryJpaEntity> hasCompanyId(UUID companyId) {
        return (root, query, cb) -> companyId == null
                ? cb.conjunction()
                : cb.equal(root.get("companyId"), companyId);
    }

    public static Specification<TimeEntryJpaEntity> hasProjectId(UUID projectId) {
        return (root, query, cb) -> projectId == null
                ? cb.conjunction()
                : cb.equal(root.get("projectId"), projectId);
    }

    public static Specification<TimeEntryJpaEntity> nameContains(String name) {
        return (root, query, cb) -> name == null || name.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<TimeEntryJpaEntity> hasAnyTag(Collection<UUID> tagIds) {
        return (root, query, cb) -> {
            if (tagIds == null || tagIds.isEmpty()) {
                return cb.conjunction();
            }
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<TimeEntryTagJpaEntity> tag = subquery.from(TimeEntryTagJpaEntity.class);
            subquery.select(cb.literal(1));
            subquery.where(cb.and(
                    cb.equal(tag.get("entryId"), root.get("id")),
                    cb.isTrue(tag.get("active")),
                    tag.get("tagId").in(tagIds)));
            return cb.exists(subquery);
        };
    }

    public static Specification<TimeEntryJpaEntity> overlapsRange(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return cb.conjunction();
            }
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<TimeEntrySessionJpaEntity> session = subquery.from(TimeEntrySessionJpaEntity.class);
            subquery.select(cb.literal(1));

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(session.get("entryId"), root.get("id")));
            predicates.add(cb.isTrue(session.get("active")));
            if (to != null) {
                predicates.add(cb.lessThan(session.get("startTime"), to));
            }
            if (from != null) {
                predicates.add(cb.or(
                        cb.isNull(session.get("endTime")),
                        cb.greaterThan(session.get("endTime"), from)));
            }
            subquery.where(predicates.toArray(Predicate[]::new));
            return cb.exists(subquery);
        };
    }

    public static Specification<TimeEntryJpaEntity> matchesEntityFilter(Collection<UUID> userIds,
            Collection<UUID> teamIds) {
        return (root, query, cb) -> {
            boolean hasUsers = userIds != null && !userIds.isEmpty();
            boolean hasTeams = teamIds != null && !teamIds.isEmpty();
            if (!hasUsers && !hasTeams) {
                return cb.conjunction();
            }

            List<Predicate> options = new ArrayList<>();
            if (hasUsers) {
                options.add(root.get("userId").in(userIds));
            }
            if (hasTeams) {
                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<TeamMembershipJpaEntity> member = subquery.from(TeamMembershipJpaEntity.class);
                subquery.select(cb.literal(1));
                subquery.where(cb.and(
                        member.get("teamId").in(teamIds),
                        cb.equal(member.get("userId"), root.get("userId")),
                        cb.isTrue(member.get("approved")),
                        cb.isTrue(member.get("active"))));
                options.add(cb.exists(subquery));
            }
            return cb.or(options.toArray(Predicate[]::new));
        };
    }

    public static Specification<TimeEntryJpaEntity> visibleToLead(UUID leadId) {
        return (root, query, cb) -> {
            if (leadId == null) {
                return cb.conjunction();
            }
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<TeamMembershipJpaEntity> lead = subquery.from(TeamMembershipJpaEntity.class);
            Root<TeamMembershipJpaEntity> member = subquery.from(TeamMembershipJpaEntity.class);
            Root<ProjectTeamJpaEntity> projectTeam = subquery.from(ProjectTeamJpaEntity.class);
            subquery.select(cb.literal(1));
            subquery.where(cb.and(
                    cb.equal(lead.get("userId"), leadId),
                    cb.equal(lead.get("role"), TeamRole.LEAD),
                    cb.isTrue(lead.get("approved")),
                    cb.isTrue(lead.get("active")),
                    cb.equal(member.get("teamId"), lead.get("teamId")),
                    cb.equal(member.get("userId"), root.get("userId")),
                    cb.isTrue(member.get("approved")),
                    cb.isTrue(member.get("active")),
                    cb.equal(projectTeam.get("teamId"), lead.get("teamId")),
                    cb.equal(projectTeam.get("projectId"), root.get("projectId")),
                    cb.isTrue(projectTeam.get("active"))));
            return cb.or(cb.equal(root.get("userId"), leadId), cb.exists(subquery));
        };
    }

    public static Specification<TimeEntryJpaEntity> recentFirst() {
        return (root, query, cb) -> {
            if (!TimeEntryJpaEntity.class.isAssignableFrom(query.getResultType())) {
                return cb.conjunction();
            }
            Subquery<LocalDateTime> lastStart = query.subquery(LocalDateTime.class);
            Root<TimeEntrySessionJpaEntity> session = lastStart.from(TimeEntrySessionJpaEntity.class);
            lastStart.select(maxStartTime(cb, session));
            lastStart.where(cb.and(
                    cb.equal(session.get("entryId"), root.get("id")),
                    cb.isTrue(session.get("active"))));
            query.orderBy(
                    cb.desc(cb.coalesce(lastStart, root.<LocalDateTime>get("createdAt"))),
                    cb.desc(root.get("seqId")));
            return cb.conjunction();
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Expression<LocalDateTime> maxStartTime(CriteriaBuilder cb,
            Root<TimeEntrySessionJpaEntity> session) {
        return (Expression<LocalDateTime>) (Expression) ((HibernateCriteriaBuilder) cb)
                .max((Expression) session.get("startTime"));
    }
}
