package com.vmarcante.time_tracker.core.infraestructure.team.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamJpaRepository extends JpaRepository<TeamJpaEntity, UUID> {

    List<TeamJpaEntity> findByCompanyIdAndActiveTrue(UUID companyId);

    boolean existsByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);

    boolean existsByCompanyIdAndNameIgnoreCaseAndActiveTrue(UUID companyId, String name);
}
