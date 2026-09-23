package com.vmarcante.time_tracker.core.application.company.dto.output;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;

public record CompanyMemberOutputDTO(
        UUID membershipId,
        UUID userId,
        String name,
        CompanyRole role,
        MembershipOrigin origin,
        Boolean approved,
        LocalDateTime approvedAt,
        UUID approvedBy,
        String approvedByName,
        LocalDateTime createdAt) {

    public static CompanyMemberOutputDTO from(CompanyMembership membership, String memberName) {
        return from(membership, memberName, null);
    }

    public static CompanyMemberOutputDTO from(
            CompanyMembership membership, String memberName, String approverName) {
        return new CompanyMemberOutputDTO(
                membership.getId(),
                membership.getUserId(),
                memberName,
                membership.getRole(),
                membership.getOrigin(),
                membership.getApproved(),
                membership.getApprovedAt(),
                membership.getApprovedBy(),
                approverName,
                membership.getCreatedAt());
    }
}
