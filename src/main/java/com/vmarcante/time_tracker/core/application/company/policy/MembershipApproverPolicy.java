package com.vmarcante.time_tracker.core.application.company.policy;

import java.util.List;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;

@Component
public class MembershipApproverPolicy {

    public List<CompanyMembership> selectApprovers(List<CompanyMembership> memberships) {
        return memberships.stream()
                .filter(m -> m.getRole() == CompanyRole.OWNER || m.getRole() == CompanyRole.ADMIN)
                .toList();
    }
}
