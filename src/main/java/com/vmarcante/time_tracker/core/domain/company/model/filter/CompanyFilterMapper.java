package com.vmarcante.time_tracker.core.domain.company.model.filter;

import java.util.UUID;

public final class CompanyFilterMapper {

    private CompanyFilterMapper() {
    }

    public static CompanyFilter byDocument(String document) {
        CompanyFilter filter = new CompanyFilter();
        filter.setDocument(document);
        return filter;
    }

    public static CompanyFilter activeMemberOf(UUID userId) {
        CompanyFilter filter = new CompanyFilter();
        filter.setMemberUserId(userId);
        filter.setActive(true);
        return filter;
    }
}
