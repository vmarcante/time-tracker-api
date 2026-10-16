package com.vmarcante.time_tracker.core.domain.company.model.filter;

import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;

public final class CompanyMembershipFilterMapper {

    private CompanyMembershipFilterMapper() {
    }


    public static CompanyMembershipFilter activeMembership(UUID userId, UUID companyId) {
        CompanyMembershipFilter filter = new CompanyMembershipFilter();
        filter.setUserId(userId);
        filter.setCompanyId(companyId);
        filter.setActive(true);
        return filter;
    }

    public static CompanyMembershipFilter approvedMembership(UUID userId, UUID companyId) {
        CompanyMembershipFilter filter = new CompanyMembershipFilter();
        filter.setUserId(userId);
        filter.setCompanyId(companyId);
        filter.setApproved(true);
        filter.setActive(true);
        return filter;
    }

    public static CompanyMembershipFilter pendingMembership(UUID userId, UUID companyId) {
        CompanyMembershipFilter filter = new CompanyMembershipFilter();
        filter.setUserId(userId);
        filter.setCompanyId(companyId);
        filter.setApproved(false);
        filter.setActive(true);
        return filter;
    }

    public static CompanyMembershipFilter pendingByUser(UUID userId, MembershipOrigin origin) {
        CompanyMembershipFilter filter = new CompanyMembershipFilter();
        filter.setUserId(userId);
        filter.setApproved(false);
        filter.setActive(true);
        filter.setOrigin(origin);
        return filter;
    }

    public static CompanyMembershipFilter approvedByCompany(UUID companyId) {
        CompanyMembershipFilter filter = new CompanyMembershipFilter();
        filter.setCompanyId(companyId);
        filter.setApproved(true);
        filter.setActive(true);
        return filter;
    }

    public static CompanyMembershipFilter pendingByCompany(UUID companyId) {
        CompanyMembershipFilter filter = new CompanyMembershipFilter();
        filter.setCompanyId(companyId);
        filter.setApproved(false);
        filter.setActive(true);
        return filter;
    }

    public static CompanyMembershipFilter resolvedByCompany(UUID companyId) {
        CompanyMembershipFilter filter = new CompanyMembershipFilter();
        filter.setCompanyId(companyId);
        filter.setResolved(true);
        return filter;
    }

    public static CompanyMembershipFilter approvedByUser(UUID userId) {
        CompanyMembershipFilter filter = new CompanyMembershipFilter();
        filter.setUserId(userId);
        filter.setApproved(true);
        filter.setActive(true);
        return filter;
    }
}
