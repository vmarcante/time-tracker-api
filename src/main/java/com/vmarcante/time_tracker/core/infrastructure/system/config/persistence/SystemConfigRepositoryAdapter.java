package com.vmarcante.time_tracker.core.infrastructure.system.config.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.vmarcante.time_tracker.core.domain.system.config.model.filter.SystemConfigFilterMapper;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.system.config.enums.SystemConfigKey;
import com.vmarcante.time_tracker.core.domain.system.config.model.SystemConfig;
import com.vmarcante.time_tracker.core.domain.system.config.model.filter.SystemConfigFilter;
import com.vmarcante.time_tracker.core.domain.system.config.repository.SystemConfigRepository;
import com.vmarcante.time_tracker.core.infrastructure.system.config.mapper.SystemConfigPersistenceMapper;

@Component
public class SystemConfigRepositoryAdapter implements SystemConfigRepository {

    private final SystemConfigJpaRepository systemConfigJpaRepository;

    public SystemConfigRepositoryAdapter(SystemConfigJpaRepository systemConfigJpaRepository) {
        this.systemConfigJpaRepository = systemConfigJpaRepository;
    }

    @Override
    public Optional<SystemConfig> findOne(SystemConfigFilter filter) {
        return systemConfigJpaRepository
                .findOne(SystemConfigRepositorySpecification.withFilter(filter))
                .map(SystemConfigPersistenceMapper::toDomain);
    }

    @Override
    public List<SystemConfig> search(SystemConfigFilter filter) {
        return systemConfigJpaRepository
                .findAll(SystemConfigRepositorySpecification.withFilter(filter))
                .stream()
                .map(SystemConfigPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<SystemConfig> findByKey(SystemConfigKey key) {
        SystemConfigFilter filter = SystemConfigFilterMapper.byKey(key.getKey());
        return findOne(filter);
    }

    @Override
    public Map<SystemConfigKey, SystemConfig> findByKeys(List<SystemConfigKey> keys) {
        SystemConfigFilter filter = SystemConfigFilterMapper.byKeys(keys.stream().map(SystemConfigKey::getKey).toList());

        return search(filter).stream()
                .collect(Collectors.toMap(
                        SystemConfig::getKey,
                        config -> config));
    }

    @Override
    public SystemConfig save(SystemConfig config) {
        SystemConfigJpaEntity entity = SystemConfigPersistenceMapper.toEntity(config);
        return SystemConfigPersistenceMapper.toDomain(systemConfigJpaRepository.save(entity));
    }
}
