package com.vmarcante.time_tracker.core.domain.system.config.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.vmarcante.time_tracker.core.domain.system.config.enums.SystemConfigKey;
import com.vmarcante.time_tracker.core.domain.system.config.model.SystemConfig;

public interface SystemConfigRepository {

    Optional<SystemConfig> findByKey(SystemConfigKey key);

    Map<SystemConfigKey, SystemConfig> findByKeys(List<SystemConfigKey> keys);

    SystemConfig save(SystemConfig config);
}
