package com.vmarcante.time_tracker.core.infraestructure.timeentry.persistence;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimeEntryJpaRepository extends JpaRepository<TimeEntryJpaEntity, UUID> {

    String RANGE_FILTER = "((:from IS NULL AND :to IS NULL) OR EXISTS ("
            + "SELECT 1 FROM TimeEntrySessionJpaEntity s "
            + "WHERE s.entryId = e.id AND s.active = true "
            + "AND (:to IS NULL OR s.startTime < :to) "
            + "AND (:from IS NULL OR s.endTime IS NULL OR s.endTime > :from)))";

    String LEAD_SCOPE = "EXISTS ("
            + "SELECT 1 FROM UserTeamJpaEntity lead, UserTeamJpaEntity member, ProjectTeamJpaEntity pt "
            + "WHERE lead.userId = :leadId "
            + "AND lead.role = com.vmarcante.time_tracker.core.domain.team.enums.TeamRole.LEAD "
            + "AND lead.approved = true AND lead.active = true "
            + "AND member.teamId = lead.teamId AND member.userId = e.userId "
            + "AND member.approved = true AND member.active = true "
            + "AND pt.teamId = lead.teamId AND pt.projectId = e.projectId AND pt.active = true)";

    Optional<TimeEntryJpaEntity> findByIdAndActiveTrue(UUID id);

    boolean existsByIdAndUserIdAndActiveTrue(UUID id, UUID userId);

    @Query("SELECT e FROM TimeEntryJpaEntity e WHERE e.active = true AND e.userId = :userId "
            + "AND (:projectId IS NULL OR e.projectId = :projectId) AND " + RANGE_FILTER)
    Page<TimeEntryJpaEntity> findActiveByUserId(@Param("userId") UUID userId,
            @Param("projectId") UUID projectId,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Query("SELECT e FROM TimeEntryJpaEntity e WHERE e.active = true AND e.companyId = :companyId "
            + "AND (:userId IS NULL OR e.userId = :userId) "
            + "AND (:projectId IS NULL OR e.projectId = :projectId) AND " + RANGE_FILTER)
    Page<TimeEntryJpaEntity> findActiveByCompanyId(@Param("companyId") UUID companyId,
            @Param("userId") UUID userId, @Param("projectId") UUID projectId,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Query("SELECT e FROM TimeEntryJpaEntity e WHERE e.active = true AND e.companyId = :companyId "
            + "AND (:userId IS NULL OR e.userId = :userId) "
            + "AND (:projectId IS NULL OR e.projectId = :projectId) AND " + RANGE_FILTER
            + " AND (e.userId = :leadId OR " + LEAD_SCOPE + ")")
    Page<TimeEntryJpaEntity> findActiveLeadScope(@Param("companyId") UUID companyId,
            @Param("leadId") UUID leadId, @Param("userId") UUID userId, @Param("projectId") UUID projectId,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Query("SELECT COUNT(e) > 0 FROM TimeEntryJpaEntity e WHERE e.id = :entryId AND e.active = true AND " + LEAD_SCOPE)
    boolean canLeadViewEntry(@Param("entryId") UUID entryId, @Param("leadId") UUID leadId);
}
