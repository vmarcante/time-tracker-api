package com.vmarcante.time_tracker.core.infraestructure.company.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.company.model.Company;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.infraestructure.company.mapper.CompanyPersistenceMapper;

@Component
public class CompanyRepositoryAdapter implements CompanyRepository {

    private final CompanyJpaRepository companyJpaRepository;
    private final UserCompanyJpaRepository userCompanyJpaRepository;

    public CompanyRepositoryAdapter(
            CompanyJpaRepository companyJpaRepository,
            UserCompanyJpaRepository userCompanyJpaRepository) {
        this.companyJpaRepository = companyJpaRepository;
        this.userCompanyJpaRepository = userCompanyJpaRepository;
    }

    @Override
    public Company save(Company company) {
        CompanyJpaEntity entity = CompanyPersistenceMapper.toEntity(company);
        return CompanyPersistenceMapper.toDomain(companyJpaRepository.save(entity));
    }

    @Override
    public CompanyMembership saveMembership(CompanyMembership membership) {
        UserCompanyJpaEntity entity = CompanyPersistenceMapper.toEntity(membership);
        return CompanyPersistenceMapper.toDomain(userCompanyJpaRepository.save(entity));
    }

    @Override
    public Optional<Company> findById(UUID id) {
        return companyJpaRepository.findById(id)
                .map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Company> findByDocument(String document) {
        return companyJpaRepository.findByDocument(document)
                .map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public List<Company> findAllByIds(Collection<UUID> ids) {
        return companyJpaRepository.findAllById(ids).stream()
                .map(CompanyPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Company> findActiveCompaniesByUserId(UUID userId) {
        List<UUID> companyIds = userCompanyJpaRepository.findApprovedCompanyIdsByUserId(userId);
        return companyJpaRepository.findAllById(companyIds).stream()
                .filter(e -> Boolean.TRUE.equals(e.getActive()))
                .map(CompanyPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByDocument(String document) {
        return companyJpaRepository.existsByDocument(document);
    }

    @Override
    public boolean isMember(UUID userId, UUID companyId) {
        return userCompanyJpaRepository
                .existsByUserIdAndCompanyIdAndActiveTrueAndApprovedTrue(userId, companyId);
    }

    @Override
    public boolean hasAnyActiveMembership(UUID userId, UUID companyId) {
        return userCompanyJpaRepository
                .existsByUserIdAndCompanyIdAndActiveTrue(userId, companyId);
    }

    @Override
    public Optional<CompanyMembership> findMembership(UUID userId, UUID companyId) {
        return userCompanyJpaRepository
                .findByUserIdAndCompanyIdAndActiveTrueAndApprovedTrue(userId, companyId)
                .map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public Optional<CompanyMembership> findPendingMembership(UUID userId, UUID companyId) {
        return userCompanyJpaRepository
                .findByUserIdAndCompanyIdAndActiveTrueAndApprovedFalse(userId, companyId)
                .map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public Optional<CompanyMembership> findMembershipById(UUID membershipId) {
        return userCompanyJpaRepository.findById(membershipId)
                .map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public List<CompanyMembership> findApprovedMembershipsByCompanyId(UUID companyId) {
        return userCompanyJpaRepository
                .findByCompanyIdAndActiveTrueAndApprovedTrue(companyId)
                .stream()
                .map(CompanyPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<CompanyMembership> findPendingMembershipsByCompanyId(UUID companyId) {
        return userCompanyJpaRepository
                .findByCompanyIdAndActiveTrueAndApprovedFalse(companyId)
                .stream()
                .map(CompanyPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<CompanyMembership> findPendingInvitationsByUserId(UUID userId) {
        return userCompanyJpaRepository.findByUserIdAndActiveTrueAndApprovedFalse(userId)
                .stream()
                .map(CompanyPersistenceMapper::toDomain)
                .toList();
    }
}
