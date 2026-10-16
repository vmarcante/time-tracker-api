package com.vmarcante.time_tracker.core.domain.company.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

public interface CompanyMembershipRepository {

    CompanyMembership saveMembership(CompanyMembership membership);

    Optional<CompanyMembership> findById(UUID id);

    Optional<CompanyMembership> findOne(CompanyMembershipFilter filter);

    List<CompanyMembership> search(CompanyMembershipFilter filter);

    Page<CompanyMembership> search(CompanyMembershipFilter filter, Pageable pageable);

    boolean exists(CompanyMembershipFilter filter);

    long count(CompanyMembershipFilter filter);

    List<UUID> findApprovedCompanyIdsByUserId(UUID userId);
}
