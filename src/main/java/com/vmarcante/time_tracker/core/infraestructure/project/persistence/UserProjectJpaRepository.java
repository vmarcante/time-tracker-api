package com.vmarcante.time_tracker.core.infraestructure.project.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserProjectJpaRepository extends JpaRepository<UserProjectJpaEntity, UUID> {

    boolean existsByUserIdAndProjectIdAndTeamIdAndActiveTrue(UUID userId, UUID projectId, UUID teamId);

    List<UserProjectJpaEntity> findByProjectIdAndActiveTrue(UUID projectId);

    List<UserProjectJpaEntity> findByUserIdAndTeamIdAndActiveTrue(UUID userId, UUID teamId);

    List<UserProjectJpaEntity> findByProjectIdAndTeamIdAndActiveTrue(UUID projectId, UUID teamId);

    @Query("""
            SELECT up FROM UserProjectJpaEntity up
            JOIN ProjectJpaEntity p ON p.id = up.projectId
            WHERE up.userId = :userId AND p.companyId = :companyId AND up.active = true
            """)
    List<UserProjectJpaEntity> findActiveByUserIdAndCompanyId(
            @Param("userId") UUID userId, @Param("companyId") UUID companyId);

    @Query("""
            SELECT up.projectId, COUNT(up) FROM UserProjectJpaEntity up
            WHERE up.projectId IN :projectIds AND up.active = true
            GROUP BY up.projectId
            """)
    List<Object[]> countActiveByProjectIds(@Param("projectIds") Collection<UUID> projectIds);

    @Modifying
    @Query("""
            UPDATE UserProjectJpaEntity up
            SET up.active = false, up.updatedAt = CURRENT_TIMESTAMP, up.updatedBy = :updatedBy
            WHERE up.projectId = :projectId AND up.teamId = :teamId AND up.active = true
            """)
    void deactivateByProjectIdAndTeamId(
            @Param("projectId") UUID projectId,
            @Param("teamId") UUID teamId,
            @Param("updatedBy") UUID updatedBy);

    @Modifying
    @Query("""
            UPDATE UserProjectJpaEntity up
            SET up.active = false, up.updatedAt = CURRENT_TIMESTAMP, up.updatedBy = :updatedBy
            WHERE up.userId = :userId AND up.teamId = :teamId AND up.active = true
            """)
    void deactivateByUserIdAndTeamId(
            @Param("userId") UUID userId,
            @Param("teamId") UUID teamId,
            @Param("updatedBy") UUID updatedBy);

    @Modifying
    @Query("""
            UPDATE UserProjectJpaEntity up
            SET up.active = false, up.updatedAt = CURRENT_TIMESTAMP, up.updatedBy = :updatedBy
            WHERE up.userId = :userId AND up.active = true
                AND up.projectId IN (
                    SELECT p.id FROM ProjectJpaEntity p WHERE p.companyId = :companyId
                )
            """)
    void deactivateByUserIdAndCompanyId(
            @Param("userId") UUID userId,
            @Param("companyId") UUID companyId,
            @Param("updatedBy") UUID updatedBy);

    @Modifying
    @Query("""
            UPDATE UserProjectJpaEntity up
            SET up.active = false, up.updatedAt = CURRENT_TIMESTAMP, up.updatedBy = :updatedBy
            WHERE up.projectId = :projectId AND up.active = true
            """)
    void deactivateByProjectId(@Param("projectId") UUID projectId, @Param("updatedBy") UUID updatedBy);

    @Modifying
    @Query("""
            UPDATE UserProjectJpaEntity up
            SET up.active = false, up.updatedAt = CURRENT_TIMESTAMP, up.updatedBy = :updatedBy
            WHERE up.teamId = :teamId AND up.active = true
            """)
    void deactivateByTeamId(@Param("teamId") UUID teamId, @Param("updatedBy") UUID updatedBy);
}
