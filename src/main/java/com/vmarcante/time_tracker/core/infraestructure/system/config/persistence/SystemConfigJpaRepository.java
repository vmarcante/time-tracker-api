package com.vmarcante.time_tracker.core.infraestructure.system.config.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigJpaRepository extends JpaRepository<SystemConfigJpaEntity, UUID> {

    Optional<SystemConfigJpaEntity> findByConfigKey(String configKey);

    List<SystemConfigJpaEntity> findByConfigKeyIn(List<String> configKeys);
}
