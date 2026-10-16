package com.vmarcante.time_tracker.core.infrastructure.system.config.persistence;

import java.util.UUID;

import com.vmarcante.time_tracker.base.infrastructure.persistence.BaseJpaRepository;

public interface SystemConfigJpaRepository extends BaseJpaRepository<SystemConfigJpaEntity, UUID, Integer> {
}
