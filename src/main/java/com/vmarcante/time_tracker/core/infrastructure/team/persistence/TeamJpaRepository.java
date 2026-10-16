package com.vmarcante.time_tracker.core.infrastructure.team.persistence;

import java.util.UUID;

import com.vmarcante.time_tracker.base.infrastructure.persistence.BaseJpaRepository;

public interface TeamJpaRepository extends BaseJpaRepository<TeamJpaEntity, UUID, Integer> {
}
