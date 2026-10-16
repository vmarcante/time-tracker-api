package com.vmarcante.time_tracker.core.domain.system.config.model.filter;

import java.util.Collection;

public final class SystemConfigFilterMapper {

    private SystemConfigFilterMapper() {
    }

    public static SystemConfigFilter byKey(String key) {
        SystemConfigFilter filter = new SystemConfigFilter();
        filter.setConfigKey(key);
        return filter;
    }

    public static SystemConfigFilter byKeys(Collection<String> keys) {
        SystemConfigFilter filter = new SystemConfigFilter();
        filter.setConfigKeys(keys);
        return filter;
    }
}
