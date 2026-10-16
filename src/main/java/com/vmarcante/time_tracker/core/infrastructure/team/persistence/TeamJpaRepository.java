package com.vmarcante.time_tracker.core.infrastructure.team.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.vmarcante.time_tracker.base.infrastructure.persistence.BaseJpaRepository;

public interface TeamJpaRepository extends BaseJpaRepository<TeamJpaEntity, UUID, Integer> {

    List<TeamJpaEntity> findByCompanyIdAndActiveTrue(UUID companyId);

    Page<TeamJpaEntity> findByCompanyIdAndActiveTrue(UUID companyId, Pageable pageable);

    boolean existsByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);

    boolean existsByCompanyIdAndNameIgnoreCaseAndActiveTrue(UUID companyId, String name);
}
