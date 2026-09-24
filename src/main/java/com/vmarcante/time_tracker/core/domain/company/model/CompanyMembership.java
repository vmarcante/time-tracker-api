package com.vmarcante.time_tracker.core.domain.company.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.model.BaseActiveDomainModel;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompanyMembership extends BaseActiveDomainModel<UUID, Integer> {

    private UUID userId;
    private UUID companyId;
    private CompanyRole role;
    private MembershipOrigin origin;
    private Boolean approved;
    private String requestReason;
    private LocalDateTime approvedAt;
    private UUID approvedBy;
    private UUID createdBy;
    private UUID updatedBy;
}
