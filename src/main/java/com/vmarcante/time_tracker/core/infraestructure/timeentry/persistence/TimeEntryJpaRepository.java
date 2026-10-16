package com.vmarcante.time_tracker.core.infraestructure.timeentry.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vmarcante.time_tracker.base.infraestructure.persistence.BaseJpaRepository;

public interface TimeEntryJpaRepository extends BaseJpaRepository<TimeEntryJpaEntity, UUID, Integer> {

    String LEAD_SCOPE = "EXISTS ("
            + "SELECT 1 FROM TeamMembershipJpaEntity lead, TeamMembershipJpaEntity member, ProjectTeamJpaEntity pt "
            + "WHERE lead.userId = :leadId "
            + "AND lead.role = com.vmarcante.time_tracker.core.domain.team.enums.TeamRole.LEAD "
            + "AND lead.approved = true AND lead.active = true "
            + "AND member.teamId = lead.teamId AND member.userId = e.userId "
            + "AND member.approved = true AND member.active = true "
            + "AND pt.teamId = lead.teamId AND pt.projectId = e.projectId AND pt.active = true)";

    Optional<TimeEntryJpaEntity> findByIdAndActiveTrue(UUID id);

    boolean existsByIdAndUserIdAndActiveTrue(UUID id, UUID userId);

    @Query("SELECT DISTINCT e.projectId FROM TimeEntryJpaEntity e "
            + "WHERE e.userId = :userId AND e.active = true AND e.projectId IS NOT NULL")
    List<UUID> findDistinctProjectIdsByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(e) > 0 FROM TimeEntryJpaEntity e WHERE e.id = :entryId AND e.active = true AND " + LEAD_SCOPE)
    boolean canLeadViewEntry(@Param("entryId") UUID entryId, @Param("leadId") UUID leadId);
}
