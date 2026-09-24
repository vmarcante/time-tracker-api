package com.vmarcante.time_tracker.core.domain.company.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.company.model.Company;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;

public interface CompanyRepository {

    Company save(Company company);

    CompanyMembership saveMembership(CompanyMembership membership);

    Optional<Company> findById(UUID id);

    Optional<Company> findByDocument(String document);

    Map<UUID, String> findLegalNamesByIds(Collection<UUID> ids);

    Page<Company> findActiveCompaniesByUserId(UUID userId, Pageable pageable);

    boolean existsByDocument(String document);

    boolean isMember(UUID userId, UUID companyId);

    boolean hasAnyActiveMembership(UUID userId, UUID companyId);

    boolean hasAnyApprovedMembership(UUID userId);

    Optional<CompanyMembership> findMembership(UUID userId, UUID companyId);

    Optional<CompanyMembership> findPendingMembership(UUID userId, UUID companyId);

    Optional<CompanyMembership> findMembershipById(UUID membershipId);

    Page<CompanyMembership> findApprovedMembershipsByCompanyId(UUID companyId, Pageable pageable);

    Page<CompanyMembership> findPendingMembershipsByCompanyId(UUID companyId, Pageable pageable);

    Page<CompanyMembership> findPendingInvitationsByUserId(UUID userId, Pageable pageable);

    long countPendingInvitationsByUserId(UUID userId);

    Optional<String> findPendingRequestCompanyNameByUserId(UUID userId);
}
