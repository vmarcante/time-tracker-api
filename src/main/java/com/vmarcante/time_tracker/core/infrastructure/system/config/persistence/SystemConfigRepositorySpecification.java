package com.vmarcante.time_tracker.core.infrastructure.system.config.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.core.domain.system.config.model.filter.SystemConfigFilter;

public final class SystemConfigRepositorySpecification {

    private SystemConfigRepositorySpecification() {
    }

    public static Specification<SystemConfigJpaEntity> withFilter(SystemConfigFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                hasConfigKey(filter.getConfigKey()),
                hasConfigKeyIn(filter.getConfigKeys()),
                isActive(filter.getActive())));
    }

    public static Specification<SystemConfigJpaEntity> hasConfigKey(String configKey) {
        return (root, query, cb) -> configKey == null || configKey.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("configKey"), configKey);
    }

    public static Specification<SystemConfigJpaEntity> hasConfigKeyIn(Collection<String> configKeys) {
        return (root, query, cb) -> configKeys == null || configKeys.isEmpty()
                ? cb.conjunction()
                : root.get("configKey").in(configKeys);
    }

    public static Specification<SystemConfigJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }
}
