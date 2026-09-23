package com.vmarcante.time_tracker.core.infraestructure.company.persistence;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Map<UUID, String> findLegalNamesByIds(Collection<UUID> ids) {
        return companyJpaRepository.findIdAndLegalNameByIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (String) row[1]));
    }

    @Override
    public Page<Company> findActiveCompaniesByUserId(UUID userId, Pageable pageable) {
        return userCompanyJpaRepository.findActiveCompaniesByUserId(userId, pageable)
                .map(CompanyPersistenceMapper::toDomain);
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
    public boolean hasAnyApprovedMembership(UUID userId) {
        return userCompanyJpaRepository
                .existsByUserIdAndActiveTrueAndApprovedTrue(userId);
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
    public Page<CompanyMembership> findApprovedMembershipsByCompanyId(UUID companyId, Pageable pageable) {
        return userCompanyJpaRepository
                .findByCompanyIdAndActiveTrueAndApprovedTrue(companyId, pageable)
                .map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public Page<CompanyMembership> findPendingMembershipsByCompanyId(UUID companyId, Pageable pageable) {
        return userCompanyJpaRepository
                .findByCompanyIdAndActiveTrueAndApprovedFalse(companyId, pageable)
                .map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public Page<CompanyMembership> findPendingInvitationsByUserId(UUID userId, Pageable pageable) {
        return userCompanyJpaRepository.findByUserIdAndActiveTrueAndApprovedFalse(userId, pageable)
                .map(CompanyPersistenceMapper::toDomain);
    }
}
