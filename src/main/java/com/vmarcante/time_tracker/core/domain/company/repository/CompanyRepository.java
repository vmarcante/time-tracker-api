package com.vmarcante.time_tracker.core.domain.company.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.company.model.Company;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;

public interface CompanyRepository {

    Company save(Company company);

    CompanyMembership saveMembership(CompanyMembership membership);

    Optional<Company> findById(UUID id);

    Optional<Company> findByDocument(String document);

    List<Company> findAllByIds(Collection<UUID> ids);

    List<Company> findActiveCompaniesByUserId(UUID userId);

    boolean existsByDocument(String document);

    boolean isMember(UUID userId, UUID companyId);

    boolean hasAnyActiveMembership(UUID userId, UUID companyId);

    Optional<CompanyMembership> findMembership(UUID userId, UUID companyId);

    Optional<CompanyMembership> findPendingMembership(UUID userId, UUID companyId);

    Optional<CompanyMembership> findMembershipById(UUID membershipId);

    List<CompanyMembership> findApprovedMembershipsByCompanyId(UUID companyId);

    List<CompanyMembership> findPendingMembershipsByCompanyId(UUID companyId);

    List<CompanyMembership> findPendingInvitationsByUserId(UUID userId);
}
