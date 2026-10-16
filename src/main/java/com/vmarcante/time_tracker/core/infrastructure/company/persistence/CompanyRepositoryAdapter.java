package com.vmarcante.time_tracker.core.infrastructure.company.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.company.model.Company;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyFilter;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.infrastructure.company.mapper.CompanyPersistenceMapper;

@Component
public class CompanyRepositoryAdapter implements CompanyRepository {

    private final CompanyJpaRepository companyJpaRepository;

    public CompanyRepositoryAdapter(CompanyJpaRepository companyJpaRepository) {
        this.companyJpaRepository = companyJpaRepository;
    }

    @Override
    public Company save(Company company) {
        CompanyJpaEntity entity = CompanyPersistenceMapper.toEntity(company);
        return CompanyPersistenceMapper.toDomain(companyJpaRepository.save(entity));
    }

    @Override
    public Optional<Company> findById(UUID id) {
        return companyJpaRepository.findById(id)
                .map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Company> findOne(CompanyFilter filter) {
        return companyJpaRepository.findOne(spec(filter))
                .map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public List<Company> search(CompanyFilter filter) {
        return companyJpaRepository.findAll(spec(filter)).stream()
                .map(CompanyPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Company> search(CompanyFilter filter, Pageable pageable) {
        return companyJpaRepository.findAll(spec(filter), pageable)
                .map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public boolean exists(CompanyFilter filter) {
        return companyJpaRepository.exists(spec(filter));
    }

    @Override
    public Map<UUID, String> findLegalNamesByIds(Collection<UUID> ids) {
        return companyJpaRepository.findIdAndLegalNameByIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (String) row[1]));
    }

    private Specification<CompanyJpaEntity> spec(CompanyFilter filter) {
        return CompanyRepositorySpecification.withFilter(filter);
    }
}
