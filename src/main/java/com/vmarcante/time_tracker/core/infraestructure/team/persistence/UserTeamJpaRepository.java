package com.vmarcante.time_tracker.core.infraestructure.team.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vmarcante.time_tracker.core.domain.team.enums.TeamRole;

public interface UserTeamJpaRepository extends JpaRepository<UserTeamJpaEntity, UUID> {

    Optional<UserTeamJpaEntity> findByUserIdAndTeamIdAndActiveTrueAndApprovedTrue(
            UUID userId, UUID teamId);

    Optional<UserTeamJpaEntity> findByTeamIdAndRoleAndActiveTrueAndApprovedTrue(
            UUID teamId, TeamRole role);

    boolean existsByUserIdAndTeamIdAndActiveTrueAndApprovedTrue(UUID userId, UUID teamId);

    boolean existsByUserIdAndTeamIdAndRoleAndActiveTrueAndApprovedTrue(
            UUID userId, UUID teamId, TeamRole role);

    boolean existsByUserIdAndTeamIdAndActiveTrue(UUID userId, UUID teamId);

    List<UserTeamJpaEntity> findByTeamIdAndActiveTrueAndApprovedTrue(UUID teamId);

    @Query("""
            SELECT ut.teamId FROM UserTeamJpaEntity ut
            JOIN TeamJpaEntity t ON t.id = ut.teamId
            WHERE ut.userId = :userId AND t.companyId = :companyId
                AND ut.active = true AND ut.approved = true AND t.active = true
            """)
    List<UUID> findApprovedTeamIdsByUserIdAndCompanyId(
            @Param("userId") UUID userId, @Param("companyId") UUID companyId);

    @Query("""
            SELECT ut.teamId, COUNT(ut) FROM UserTeamJpaEntity ut
            WHERE ut.teamId IN :teamIds AND ut.active = true AND ut.approved = true
            GROUP BY ut.teamId
            """)
    List<Object[]> countApprovedMembersByTeamIds(@Param("teamIds") Collection<UUID> teamIds);

    @Query("""
            SELECT ut.teamId, ut.userId FROM UserTeamJpaEntity ut
            WHERE ut.teamId IN :teamIds AND ut.role = 'LEAD'
                AND ut.active = true AND ut.approved = true
            """)
    List<Object[]> findLeadUserIdsByTeamIds(@Param("teamIds") Collection<UUID> teamIds);

    @Modifying
    @Query("""
            UPDATE UserTeamJpaEntity ut
            SET ut.active = false, ut.updatedAt = CURRENT_TIMESTAMP, ut.updatedBy = :updatedBy
            WHERE ut.teamId = :teamId AND ut.active = true
            """)
    void deactivateByTeamId(@Param("teamId") UUID teamId, @Param("updatedBy") UUID updatedBy);
}
