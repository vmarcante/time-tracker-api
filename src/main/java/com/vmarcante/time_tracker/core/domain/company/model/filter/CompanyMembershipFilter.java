package com.vmarcante.time_tracker.core.domain.company.model.filter;

import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;

import lombok.Data;

@Data
public class CompanyMembershipFilter {
    private UUID id;
    private UUID userId;
    private UUID companyId;
    private CompanyRole role;
    private MembershipOrigin origin;
    private Boolean approved;
    private Boolean active;
    private Boolean resolved;
}
