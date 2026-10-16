package com.vmarcante.time_tracker.core.domain.company.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.company.model.Company;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyFilter;

public interface CompanyRepository {

    Company save(Company company);

    Optional<Company> findById(UUID id);

    Optional<Company> findOne(CompanyFilter filter);

    List<Company> search(CompanyFilter filter);

    Page<Company> search(CompanyFilter filter, Pageable pageable);

    boolean exists(CompanyFilter filter);

    Map<UUID, String> findLegalNamesByIds(Collection<UUID> ids);
}
