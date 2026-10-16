package com.vmarcante.time_tracker.core.infraestructure.timeentry.persistence;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimeEntryJpaRepository extends JpaRepository<TimeEntryJpaEntity, UUID> {

    String RANGE_FILTER = "((cast(:from as LocalDateTime) IS NULL AND cast(:to as LocalDateTime) IS NULL) OR EXISTS ("
            + "SELECT 1 FROM TimeEntrySessionJpaEntity s "
            + "WHERE s.entryId = e.id AND s.active = true "
            + "AND (cast(:to as LocalDateTime) IS NULL OR s.startTime < :to) "
            + "AND (cast(:from as LocalDateTime) IS NULL OR s.endTime IS NULL OR s.endTime > :from)))";

    String LEAD_SCOPE = "EXISTS ("
            + "SELECT 1 FROM UserTeamJpaEntity lead, UserTeamJpaEntity member, ProjectTeamJpaEntity pt "
            + "WHERE lead.userId = :leadId "
            + "AND lead.role = com.vmarcante.time_tracker.core.domain.team.enums.TeamRole.LEAD "
            + "AND lead.approved = true AND lead.active = true "
            + "AND member.teamId = lead.teamId AND member.userId = e.userId "
            + "AND member.approved = true AND member.active = true "
            + "AND pt.teamId = lead.teamId AND pt.projectId = e.projectId AND pt.active = true)";

    String ENTITY_FILTER = "("
            + "(:#{#userIds == null || #userIds.isEmpty()} = true "
            + "AND :#{#teamIds == null || #teamIds.isEmpty()} = true) "
            + "OR (:#{#userIds == null || #userIds.isEmpty()} = false AND e.userId IN :userIds) "
            + "OR (:#{#teamIds == null || #teamIds.isEmpty()} = false AND EXISTS ("
            + "SELECT 1 FROM UserTeamJpaEntity tm "
            + "WHERE tm.teamId IN :teamIds AND tm.userId = e.userId "
            + "AND tm.approved = true AND tm.active = true)))";

    String RECENT_FIRST_ORDER = " ORDER BY COALESCE(("
            + "SELECT MAX(s.startTime) FROM TimeEntrySessionJpaEntity s "
            + "WHERE s.entryId = e.id AND s.active = true), e.createdAt) DESC, e.seqId DESC";

    Optional<TimeEntryJpaEntity> findByIdAndActiveTrue(UUID id);

    boolean existsByIdAndUserIdAndActiveTrue(UUID id, UUID userId);

    @Query("SELECT e FROM TimeEntryJpaEntity e WHERE e.active = true AND e.userId = :userId "
            + "AND (:projectId IS NULL OR e.projectId = :projectId) "
            + "AND (:#{#tagIds == null || #tagIds.isEmpty()} = true OR EXISTS ("
            + "SELECT 1 FROM TimeEntryTagJpaEntity et "
            + "WHERE et.entryId = e.id AND et.active = true AND et.tagId IN :tagIds)) "
            + "AND " + RANGE_FILTER + RECENT_FIRST_ORDER)
    Page<TimeEntryJpaEntity> findActiveByUserId(@Param("userId") UUID userId,
            @Param("projectId") UUID projectId, @Param("tagIds") Collection<UUID> tagIds,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Query("SELECT DISTINCT e.projectId FROM TimeEntryJpaEntity e "
            + "WHERE e.userId = :userId AND e.active = true AND e.projectId IS NOT NULL")
    List<UUID> findDistinctProjectIdsByUserId(@Param("userId") UUID userId);

    @Query("SELECT e FROM TimeEntryJpaEntity e WHERE e.active = true AND e.companyId = :companyId "
            + "AND " + ENTITY_FILTER + " "
            + "AND (:projectId IS NULL OR e.projectId = :projectId) AND " + RANGE_FILTER
            + RECENT_FIRST_ORDER)
    Page<TimeEntryJpaEntity> findActiveByCompanyId(@Param("companyId") UUID companyId,
            @Param("userIds") Collection<UUID> userIds, @Param("projectId") UUID projectId,
            @Param("teamIds") Collection<UUID> teamIds,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Query("SELECT e FROM TimeEntryJpaEntity e WHERE e.active = true AND e.companyId = :companyId "
            + "AND " + ENTITY_FILTER + " "
            + "AND (:projectId IS NULL OR e.projectId = :projectId) AND " + RANGE_FILTER
            + " AND (e.userId = :leadId OR " + LEAD_SCOPE + ")" + RECENT_FIRST_ORDER)
    Page<TimeEntryJpaEntity> findActiveLeadScope(@Param("companyId") UUID companyId,
            @Param("leadId") UUID leadId, @Param("userIds") Collection<UUID> userIds,
            @Param("projectId") UUID projectId, @Param("teamIds") Collection<UUID> teamIds,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Query("SELECT e FROM TimeEntryJpaEntity e WHERE e.active = true AND e.companyId = :companyId "
            + "AND " + ENTITY_FILTER + " "
            + "AND (:projectId IS NULL OR e.projectId = :projectId) AND " + RANGE_FILTER)
    List<TimeEntryJpaEntity> findAllActiveByCompanyId(@Param("companyId") UUID companyId,
            @Param("userIds") Collection<UUID> userIds, @Param("projectId") UUID projectId,
            @Param("teamIds") Collection<UUID> teamIds,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT e FROM TimeEntryJpaEntity e WHERE e.active = true AND e.companyId = :companyId "
            + "AND " + ENTITY_FILTER + " "
            + "AND (:projectId IS NULL OR e.projectId = :projectId) AND " + RANGE_FILTER
            + " AND (e.userId = :leadId OR " + LEAD_SCOPE + ")")
    List<TimeEntryJpaEntity> findAllActiveLeadScope(@Param("companyId") UUID companyId,
            @Param("leadId") UUID leadId, @Param("userIds") Collection<UUID> userIds,
            @Param("projectId") UUID projectId, @Param("teamIds") Collection<UUID> teamIds,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(e) > 0 FROM TimeEntryJpaEntity e WHERE e.id = :entryId AND e.active = true AND " + LEAD_SCOPE)
    boolean canLeadViewEntry(@Param("entryId") UUID entryId, @Param("leadId") UUID leadId);
}
