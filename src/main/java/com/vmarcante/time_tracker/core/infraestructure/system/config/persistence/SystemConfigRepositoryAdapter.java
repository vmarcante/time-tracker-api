package com.vmarcante.time_tracker.core.infraestructure.system.config.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.system.config.enums.SystemConfigKey;
import com.vmarcante.time_tracker.core.domain.system.config.model.SystemConfig;
import com.vmarcante.time_tracker.core.domain.system.config.repository.SystemConfigRepository;
import com.vmarcante.time_tracker.core.infraestructure.system.config.mapper.SystemConfigPersistenceMapper;

@Component
public class SystemConfigRepositoryAdapter implements SystemConfigRepository {

    private final SystemConfigJpaRepository systemConfigJpaRepository;

    public SystemConfigRepositoryAdapter(SystemConfigJpaRepository systemConfigJpaRepository) {
        this.systemConfigJpaRepository = systemConfigJpaRepository;
    }

    @Override
    public Optional<SystemConfig> findByKey(SystemConfigKey key) {
        return systemConfigJpaRepository
                .findByConfigKey(key.getKey())
                .map(SystemConfigPersistenceMapper::toDomain);
    }

    @Override
    public Map<SystemConfigKey, SystemConfig> findByKeys(List<SystemConfigKey> keys) {
        List<String> keyStrings = keys.stream()
                .map(key -> key.getKey())
                .toList();

        return systemConfigJpaRepository
                .findByConfigKeyIn(keyStrings)
                .stream()
                .map(entity -> SystemConfigPersistenceMapper.toDomain(entity))
                .collect(Collectors.toMap(
                        config -> config.getKey(),
                        config -> config));
    }

    @Override
    public SystemConfig save(SystemConfig config) {
        SystemConfigJpaEntity entity = SystemConfigPersistenceMapper.toEntity(config);
        return SystemConfigPersistenceMapper.toDomain(systemConfigJpaRepository.save(entity));
    }
}
