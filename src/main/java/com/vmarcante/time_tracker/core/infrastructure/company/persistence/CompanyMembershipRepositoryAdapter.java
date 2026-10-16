package com.vmarcante.time_tracker.core.infrastructure.company.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.infrastructure.company.mapper.CompanyMembershipPersistenceMapper;

@Component
public class CompanyMembershipRepositoryAdapter implements CompanyMembershipRepository {

    private final CompanyMembershipJpaRepository companyMembershipJpaRepository;

    public CompanyMembershipRepositoryAdapter(CompanyMembershipJpaRepository companyMembershipJpaRepository) {
        this.companyMembershipJpaRepository = companyMembershipJpaRepository;
    }

    @Override
    public CompanyMembership saveMembership(CompanyMembership membership) {
        CompanyMembershipJpaEntity entity = CompanyMembershipPersistenceMapper.toEntity(membership);
        return CompanyMembershipPersistenceMapper.toDomain(companyMembershipJpaRepository.save(entity));
    }

    @Override
    public Optional<CompanyMembership> findById(UUID id) {
        return companyMembershipJpaRepository.findById(id)
                .map(CompanyMembershipPersistenceMapper::toDomain);
    }

    @Override
    public Optional<CompanyMembership> findOne(CompanyMembershipFilter filter) {
        return companyMembershipJpaRepository
                .findOne(CompanyMembershipRepositorySpecification.withFilter(filter))
                .map(CompanyMembershipPersistenceMapper::toDomain);
    }

    @Override
    public List<CompanyMembership> search(CompanyMembershipFilter filter) {
        return companyMembershipJpaRepository
                .findAll(CompanyMembershipRepositorySpecification.withFilter(filter))
                .stream()
                .map(CompanyMembershipPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Page<CompanyMembership> search(CompanyMembershipFilter filter, Pageable pageable) {
        return companyMembershipJpaRepository
                .findAll(CompanyMembershipRepositorySpecification.withFilter(filter), pageable)
                .map(CompanyMembershipPersistenceMapper::toDomain);
    }

    @Override
    public boolean exists(CompanyMembershipFilter filter) {
        return companyMembershipJpaRepository
                .exists(CompanyMembershipRepositorySpecification.withFilter(filter));
    }

    @Override
    public long count(CompanyMembershipFilter filter) {
        return companyMembershipJpaRepository
                .count(CompanyMembershipRepositorySpecification.withFilter(filter));
    }

    @Override
    public List<UUID> findApprovedCompanyIdsByUserId(UUID userId) {
        return companyMembershipJpaRepository.findApprovedCompanyIdsByUserId(userId);
    }
}
