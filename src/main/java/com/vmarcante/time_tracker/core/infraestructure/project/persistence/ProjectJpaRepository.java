package com.vmarcante.time_tracker.core.infraestructure.project.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectJpaRepository extends JpaRepository<ProjectJpaEntity, UUID> {

    List<ProjectJpaEntity> findByCompanyIdAndActiveTrue(UUID companyId);

    boolean existsByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);

    boolean existsByCompanyIdAndNameIgnoreCaseAndActiveTrue(UUID companyId, String name);

    @Query("""
            SELECT p.id, p.name FROM ProjectJpaEntity p
            WHERE p.id IN :ids
            """)
    List<Object[]> findIdAndNameByIds(@Param("ids") Collection<UUID> ids);

    @Query("""
            SELECT DISTINCT up.projectId FROM UserProjectJpaEntity up
            JOIN ProjectJpaEntity p ON p.id = up.projectId
            WHERE up.userId = :userId AND p.companyId = :companyId
                AND up.active = true AND p.active = true
            """)
    List<UUID> findAssignedProjectIdsByUserIdAndCompanyId(
            @Param("userId") UUID userId, @Param("companyId") UUID companyId);
}
