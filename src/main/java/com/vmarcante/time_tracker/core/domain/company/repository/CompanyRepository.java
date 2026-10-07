package com.vmarcante.time_tracker.core.domain.company.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.company.model.Company;

public interface CompanyRepository {

    Company save(Company company);

    Optional<Company> findById(UUID id);

    Optional<Company> findByDocument(String document);

    Map<UUID, String> findLegalNamesByIds(Collection<UUID> ids);

    Page<Company> findActiveCompaniesByUserId(UUID userId, Pageable pageable);

    boolean existsByDocument(String document);
}
