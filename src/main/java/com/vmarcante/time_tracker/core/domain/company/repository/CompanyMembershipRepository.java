package com.vmarcante.time_tracker.core.domain.company.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;

public interface CompanyMembershipRepository {

    CompanyMembership saveMembership(CompanyMembership membership);

    boolean isMember(UUID userId, UUID companyId);

    boolean hasAnyActiveMembership(UUID userId, UUID companyId);

    boolean hasAnyApprovedMembership(UUID userId);

    Optional<CompanyMembership> findMembership(UUID userId, UUID companyId);

    Optional<CompanyMembership> findPendingMembership(UUID userId, UUID companyId);

    Optional<CompanyMembership> findMembershipById(UUID membershipId);

    Page<CompanyMembership> findApprovedMembershipsByCompanyId(UUID companyId, Pageable pageable);

    Page<CompanyMembership> findPendingMembershipsByCompanyId(UUID companyId, Pageable pageable);

    Page<CompanyMembership> findPendingInvitationsByUserId(UUID userId, Pageable pageable);

    Page<CompanyMembership> findResolvedMembershipsByCompanyId(UUID companyId, Pageable pageable);

    Optional<CompanyMembership> findApprovedMembershipByUserId(UUID userId);

    long countPendingInvitationsByUserId(UUID userId);

    long countPendingMembershipsByCompanyId(UUID companyId);

    Optional<String> findPendingRequestCompanyNameByUserId(UUID userId);
}
