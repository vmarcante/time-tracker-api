package com.vmarcante.time_tracker.core.infraestructure.project.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vmarcante.time_tracker.core.infraestructure.team.persistence.TeamJpaEntity;

public interface ProjectTeamJpaRepository extends JpaRepository<ProjectTeamJpaEntity, UUID> {

    Optional<ProjectTeamJpaEntity> findByProjectIdAndTeamIdAndActiveTrue(UUID projectId, UUID teamId);

    boolean existsByProjectIdAndTeamIdAndActiveTrue(UUID projectId, UUID teamId);

    @Query("""
            SELECT pt.teamId FROM ProjectTeamJpaEntity pt
            JOIN TeamJpaEntity t ON t.id = pt.teamId
            WHERE pt.projectId = :projectId AND pt.active = true AND t.active = true
            """)
    List<UUID> findActiveTeamIdsByProjectId(@Param("projectId") UUID projectId);

    @Query("""
            SELECT pt.projectId FROM ProjectTeamJpaEntity pt
            JOIN ProjectJpaEntity p ON p.id = pt.projectId
            WHERE pt.teamId = :teamId AND pt.active = true AND p.active = true
            """)
    List<UUID> findActiveProjectIdsByTeamId(@Param("teamId") UUID teamId);

    @Query("""
            SELECT p FROM ProjectTeamJpaEntity pt
            JOIN ProjectJpaEntity p ON p.id = pt.projectId
            WHERE pt.teamId = :teamId AND pt.active = true AND p.active = true
            """)
    Page<ProjectJpaEntity> findActiveProjectsByTeamId(
            @Param("teamId") UUID teamId, Pageable pageable);

    @Query("""
            SELECT t FROM ProjectTeamJpaEntity pt
            JOIN TeamJpaEntity t ON t.id = pt.teamId
            WHERE pt.projectId = :projectId AND pt.active = true AND t.active = true
            """)
    Page<TeamJpaEntity> findActiveTeamsByProjectId(
            @Param("projectId") UUID projectId, Pageable pageable);

    @Query("""
            SELECT pt.projectId, COUNT(pt) FROM ProjectTeamJpaEntity pt
            JOIN TeamJpaEntity t ON t.id = pt.teamId
            WHERE pt.projectId IN :projectIds AND pt.active = true AND t.active = true
            GROUP BY pt.projectId
            """)
    List<Object[]> countActiveTeamsByProjectIds(@Param("projectIds") Collection<UUID> projectIds);

    @Modifying
    @Query("""
            UPDATE ProjectTeamJpaEntity pt
            SET pt.active = false, pt.updatedAt = CURRENT_TIMESTAMP, pt.updatedBy = :updatedBy
            WHERE pt.projectId = :projectId AND pt.active = true
            """)
    void deactivateByProjectId(@Param("projectId") UUID projectId, @Param("updatedBy") UUID updatedBy);

    @Modifying
    @Query("""
            UPDATE ProjectTeamJpaEntity pt
            SET pt.active = false, pt.updatedAt = CURRENT_TIMESTAMP, pt.updatedBy = :updatedBy
            WHERE pt.teamId = :teamId AND pt.active = true
            """)
    void deactivateByTeamId(@Param("teamId") UUID teamId, @Param("updatedBy") UUID updatedBy);
}
