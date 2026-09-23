package com.vmarcante.time_tracker.core.domain.company.enums;

public enum CompanyRole {
    OWNER(4),
    ADMIN(3),
    MANAGER(2),
    MEMBER(1);

    private final int level;

    CompanyRole(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean canManage(CompanyRole target) {
        return this.level > target.level;
    }
}
