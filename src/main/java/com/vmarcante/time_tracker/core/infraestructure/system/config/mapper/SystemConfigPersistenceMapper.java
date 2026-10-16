package com.vmarcante.time_tracker.core.infraestructure.system.config.mapper;

import com.vmarcante.time_tracker.core.domain.system.config.enums.SystemConfigKey;
import com.vmarcante.time_tracker.core.domain.system.config.model.SystemConfig;
import com.vmarcante.time_tracker.core.infraestructure.system.config.persistence.SystemConfigJpaEntity;

public final class SystemConfigPersistenceMapper {

    private SystemConfigPersistenceMapper() {
    }

    public static SystemConfig toDomain(SystemConfigJpaEntity entity) {
        SystemConfig config = new SystemConfig();
        config.setId(entity.getId());
        config.setSeqId(entity.getSeqId());
        config.setCreatedAt(entity.getCreatedAt());
        config.setUpdatedAt(entity.getUpdatedAt());
        config.setActive(entity.getActive());
        config.setKey(SystemConfigKey.fromKey(entity.getConfigKey()));
        config.setValue(entity.getConfigValue());
        config.setValueType(entity.getValueType());
        config.setDescription(entity.getDescription());
        return config;
    }

    public static SystemConfigJpaEntity toEntity(SystemConfig config) {
        SystemConfigJpaEntity entity = new SystemConfigJpaEntity();
        entity.setId(config.getId());
        entity.setActive(config.getActive());
        entity.setConfigKey(config.getKey().getKey());
        entity.setConfigValue(config.getValue());
        entity.setValueType(config.getValueType());
        entity.setDescription(config.getDescription());
        return entity;
    }
}
