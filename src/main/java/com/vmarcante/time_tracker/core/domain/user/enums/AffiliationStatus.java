package com.vmarcante.time_tracker.core.domain.user.enums;

public enum AffiliationStatus {
    PENDING("user.affiliation.pending"),
    INDEPENDENT("user.affiliation.independent"),
    COMPANY("user.affiliation.company");

    private final String key;

    AffiliationStatus(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
