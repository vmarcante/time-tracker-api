package com.vmarcante.time_tracker.core.domain.system.config.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.model.BaseActiveDomainModel;
import com.vmarcante.time_tracker.core.domain.system.config.enums.SystemConfigKey;
import com.vmarcante.time_tracker.core.domain.system.config.enums.SystemConfigValueType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SystemConfig extends BaseActiveDomainModel<UUID, Integer> {

    private SystemConfigKey key;
    private String value;
    private SystemConfigValueType valueType;
    private String description;

    /**
     * Returns the value split by ";" as a trimmed, non-empty list.
     * Only meaningful when valueType is STRING_LIST.
     */
    public List<String> getValueAsList() {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(";"))
                .map(s -> s.trim())
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * Returns the value parsed as a boolean.
     * Only meaningful when valueType is BOOLEAN.
     * Null, blank or unparseable values return {@code false}.
     */
    public boolean getValueAsBoolean() {
        if (value == null || value.isBlank()) {
            return false;
        }
        return Boolean.parseBoolean(value.trim());
    }
}
