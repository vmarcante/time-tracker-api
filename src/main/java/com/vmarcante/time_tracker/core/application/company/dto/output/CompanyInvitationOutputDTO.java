package com.vmarcante.time_tracker.core.application.company.dto.output;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;

public record CompanyInvitationOutputDTO(
        UUID membershipId,
        UUID companyId,
        String companyName,
        CompanyRole role,
        LocalDateTime createdAt) {

    public static CompanyInvitationOutputDTO from(CompanyMembership membership, String companyName) {
        return new CompanyInvitationOutputDTO(
                membership.getId(),
                membership.getCompanyId(),
                companyName,
                membership.getRole(),
                membership.getCreatedAt());
    }
}
