package com.vmarcante.time_tracker.core.application.company.dto.output;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.person.model.Person;

public record CompanyMemberOutputDTO(
        UUID membershipId,
        UUID userId,
        String name,
        String email,
        Integer age,
        CompanyRole role,
        MembershipOrigin origin,
        Boolean approved,
        Boolean active,
        String requestReason,
        String rejectionReason,
        LocalDateTime approvedAt,
        UUID approvedBy,
        String approvedByName,
        String updatedByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static CompanyMemberOutputDTO from(CompanyMembership membership, String memberName) {
        return from(membership, memberName, null);
    }

    public static CompanyMemberOutputDTO from(
            CompanyMembership membership, String memberName, String approverName) {
        return new CompanyMemberOutputDTO(
                membership.getId(),
                membership.getUserId(),
                memberName,
                null,
                null,
                membership.getRole(),
                membership.getOrigin(),
                membership.getApproved(),
                membership.getActive(),
                membership.getRequestReason(),
                membership.getRejectionReason(),
                membership.getApprovedAt(),
                membership.getApprovedBy(),
                approverName,
                null,
                membership.getCreatedAt(),
                membership.getUpdatedAt());
    }

    public static CompanyMemberOutputDTO fromPerson(
            CompanyMembership membership, Person member, String approverName, String updaterName) {
        return new CompanyMemberOutputDTO(
                membership.getId(),
                membership.getUserId(),
                member != null ? member.getName() : null,
                member != null && member.getEmail() != null ? member.getEmail().address() : null,
                member != null ? member.getAge() : null,
                membership.getRole(),
                membership.getOrigin(),
                membership.getApproved(),
                membership.getActive(),
                membership.getRequestReason(),
                membership.getRejectionReason(),
                membership.getApprovedAt(),
                membership.getApprovedBy(),
                approverName,
                updaterName,
                membership.getCreatedAt(),
                membership.getUpdatedAt());
    }
}
