package com.vmarcante.time_tracker.core.infrastructure.system.config.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.vmarcante.time_tracker.base.infrastructure.persistence.BaseJpaRepository;


public interface SystemConfigJpaRepository extends BaseJpaRepository<SystemConfigJpaEntity, UUID, Integer> {

    Optional<SystemConfigJpaEntity> findByConfigKey(String configKey);

    List<SystemConfigJpaEntity> findByConfigKeyIn(List<String> configKeys);
}
