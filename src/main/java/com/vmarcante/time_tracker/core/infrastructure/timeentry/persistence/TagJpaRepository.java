package com.vmarcante.time_tracker.core.infrastructure.timeentry.persistence;

import java.util.UUID;

import com.vmarcante.time_tracker.base.infrastructure.persistence.BaseJpaRepository;

public interface TagJpaRepository extends BaseJpaRepository<TagJpaEntity, UUID, Integer> {
}
