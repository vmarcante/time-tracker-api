package com.vmarcante.time_tracker.core.domain.system.config.enums;

public enum SystemConfigKey {

    SYSTEM_EMAIL_OVERRIDE("system_email_override"),
    SYSTEM_EMAIL_OVERRIDE_INCLUDE_ORIGINAL("system_email_override_include_original");

    private final String key;

    SystemConfigKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static SystemConfigKey fromKey(String key) {
        for (SystemConfigKey value : values()) {
            if (value.key.equalsIgnoreCase(key)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown system config key: " + key);
    }
}
