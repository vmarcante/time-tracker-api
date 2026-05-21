package com.vmarcante.time_tracker.core.domain.user.enums;

public enum UserRoleType {
    ADMINISTRATOR("user.role.administrator", 3),
    MODERATOR("user.role.moderator", 2),
    USER("user.role.user", 1);

    private final String key;
    private final Integer level;

    UserRoleType(String key, Integer level) {
        this.key = key;
        this.level = level;
    }

    public String getKey() {
        return key;
    }

    public Integer getLevel() {
        return level;
    }
}
