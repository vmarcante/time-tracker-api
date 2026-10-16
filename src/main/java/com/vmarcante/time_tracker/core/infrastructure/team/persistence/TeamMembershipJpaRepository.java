package com.vmarcante.time_tracker.core.infrastructure.team.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vmarcante.time_tracker.base.infrastructure.persistence.BaseJpaRepository;
import com.vmarcante.time_tracker.core.domain.team.enums.TeamRole;

public interface TeamMembershipJpaRepository extends BaseJpaRepository<TeamMembershipJpaEntity, UUID, Integer> {

    @Query("""
            SELECT ut.teamId FROM TeamMembershipJpaEntity ut
            JOIN TeamJpaEntity t ON t.id = ut.teamId
            WHERE ut.userId = :userId AND t.companyId = :companyId
                AND ut.active = true AND ut.approved = true AND t.active = true
            """)
    List<UUID> findApprovedTeamIdsByUserIdAndCompanyId(
            @Param("userId") UUID userId, @Param("companyId") UUID companyId);

    @Query("""
            SELECT ut.teamId, COUNT(ut) FROM TeamMembershipJpaEntity ut
            WHERE ut.teamId IN :teamIds AND ut.active = true AND ut.approved = true
            GROUP BY ut.teamId
            """)
    List<Object[]> countApprovedMembersByTeamIds(@Param("teamIds") Collection<UUID> teamIds);

    @Query("""
            SELECT ut.teamId, ut.userId FROM TeamMembershipJpaEntity ut
            WHERE ut.teamId IN :teamIds AND ut.role = 'LEAD'
                AND ut.active = true AND ut.approved = true
            """)
    List<Object[]> findLeadUserIdsByTeamIds(@Param("teamIds") Collection<UUID> teamIds);

    @Modifying
    @Query("""
            UPDATE TeamMembershipJpaEntity ut
            SET ut.active = false, ut.updatedAt = CURRENT_TIMESTAMP, ut.updatedBy = :updatedBy
            WHERE ut.teamId = :teamId AND ut.active = true
            """)
    void deactivateByTeamId(@Param("teamId") UUID teamId, @Param("updatedBy") UUID updatedBy);

    @Modifying
    @Query("""
            UPDATE TeamMembershipJpaEntity ut
            SET ut.active = false, ut.updatedAt = CURRENT_TIMESTAMP, ut.updatedBy = :updatedBy
            WHERE ut.userId = :userId AND ut.active = true
                AND ut.teamId IN (
                    SELECT t.id FROM TeamJpaEntity t WHERE t.companyId = :companyId
                )
            """)
    void deactivateByUserIdAndCompanyId(
            @Param("userId") UUID userId,
            @Param("companyId") UUID companyId,
            @Param("updatedBy") UUID updatedBy);

    @Modifying
    @Query("""
            UPDATE TeamMembershipJpaEntity ut
            SET ut.role = :newRole, ut.updatedAt = CURRENT_TIMESTAMP, ut.updatedBy = :updatedBy
            WHERE ut.userId = :userId AND ut.role = :leadRole AND ut.active = true
                AND ut.teamId IN (
                    SELECT t.id FROM TeamJpaEntity t WHERE t.companyId = :companyId
                )
            """)
    void demoteLeadsByUserIdAndCompanyId(
            @Param("userId") UUID userId,
            @Param("companyId") UUID companyId,
            @Param("leadRole") TeamRole leadRole,
            @Param("newRole") TeamRole newRole,
            @Param("updatedBy") UUID updatedBy);
}
