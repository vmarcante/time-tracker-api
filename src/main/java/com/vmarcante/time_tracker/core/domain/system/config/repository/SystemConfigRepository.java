package com.vmarcante.time_tracker.core.domain.system.config.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.vmarcante.time_tracker.core.domain.system.config.enums.SystemConfigKey;
import com.vmarcante.time_tracker.core.domain.system.config.model.SystemConfig;
import com.vmarcante.time_tracker.core.domain.system.config.model.filter.SystemConfigFilter;

public interface SystemConfigRepository {

    Optional<SystemConfig> findOne(SystemConfigFilter filter);

    List<SystemConfig> search(SystemConfigFilter filter);

    Optional<SystemConfig> findByKey(SystemConfigKey key);

    Map<SystemConfigKey, SystemConfig> findByKeys(List<SystemConfigKey> keys);

    SystemConfig save(SystemConfig config);
}
