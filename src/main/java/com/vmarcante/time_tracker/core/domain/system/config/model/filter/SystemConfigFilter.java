package com.vmarcante.time_tracker.core.domain.system.config.model.filter;

import java.util.Collection;

import lombok.Data;

@Data
public class SystemConfigFilter {
    private String configKey;
    private Collection<String> configKeys;
    private Boolean active;
}
