package com.vmarcante.time_tracker.core.infraestructure.company.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.infraestructure.company.mapper.CompanyMembershipPersistenceMapper;

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
    public boolean isMember(UUID userId, UUID companyId) {
        return companyMembershipJpaRepository
                .existsByUserIdAndCompanyIdAndActiveTrueAndApprovedTrue(userId, companyId);
    }

    @Override
    public boolean hasAnyActiveMembership(UUID userId, UUID companyId) {
        return companyMembershipJpaRepository
                .existsByUserIdAndCompanyIdAndActiveTrue(userId, companyId);
    }

    @Override
    public boolean hasAnyApprovedMembership(UUID userId) {
        return companyMembershipJpaRepository
                .existsByUserIdAndActiveTrueAndApprovedTrue(userId);
    }

    @Override
    public Optional<CompanyMembership> findMembership(UUID userId, UUID companyId) {
        return companyMembershipJpaRepository
                .findByUserIdAndCompanyIdAndActiveTrueAndApprovedTrue(userId, companyId)
                .map(CompanyMembershipPersistenceMapper::toDomain);
    }

    @Override
    public Optional<CompanyMembership> findPendingMembership(UUID userId, UUID companyId) {
        return companyMembershipJpaRepository
                .findByUserIdAndCompanyIdAndActiveTrueAndApprovedFalse(userId, companyId)
                .map(CompanyMembershipPersistenceMapper::toDomain);
    }

    @Override
    public Optional<CompanyMembership> findMembershipById(UUID membershipId) {
        return companyMembershipJpaRepository.findById(membershipId)
                .map(CompanyMembershipPersistenceMapper::toDomain);
    }

    @Override
    public Page<CompanyMembership> findApprovedMembershipsByCompanyId(UUID companyId, Pageable pageable) {
        return companyMembershipJpaRepository
                .findByCompanyIdAndActiveTrueAndApprovedTrue(companyId, pageable)
                .map(CompanyMembershipPersistenceMapper::toDomain);
    }

    @Override
    public Page<CompanyMembership> findPendingMembershipsByCompanyId(UUID companyId, Pageable pageable) {
        return companyMembershipJpaRepository
                .findByCompanyIdAndActiveTrueAndApprovedFalse(companyId, pageable)
                .map(CompanyMembershipPersistenceMapper::toDomain);
    }

    @Override
    public Page<CompanyMembership> findResolvedMembershipsByCompanyId(UUID companyId, Pageable pageable) {
        return companyMembershipJpaRepository
                .findResolvedByCompanyId(companyId, pageable)
                .map(CompanyMembershipPersistenceMapper::toDomain);
    }

    @Override
    public Page<CompanyMembership> findPendingInvitationsByUserId(UUID userId, Pageable pageable) {
        return companyMembershipJpaRepository
                .findByUserIdAndActiveTrueAndApprovedFalseAndOrigin(userId, MembershipOrigin.INVITE, pageable)
                .map(CompanyMembershipPersistenceMapper::toDomain);
    }

    @Override
    public Optional<CompanyMembership> findApprovedMembershipByUserId(UUID userId) {
        return companyMembershipJpaRepository
                .findFirstByUserIdAndActiveTrueAndApprovedTrue(userId)
                .map(CompanyMembershipPersistenceMapper::toDomain);
    }

    @Override
    public long countPendingInvitationsByUserId(UUID userId) {
        return companyMembershipJpaRepository
                .countByUserIdAndActiveTrueAndApprovedFalseAndOrigin(userId, MembershipOrigin.INVITE);
    }

    @Override
    public long countPendingMembershipsByCompanyId(UUID companyId) {
        return companyMembershipJpaRepository
                .countByCompanyIdAndActiveTrueAndApprovedFalse(companyId);
    }

    @Override
    public Optional<CompanyMembership> findPendingRequestMembershipByUserId(UUID userId) {
        return companyMembershipJpaRepository
                .findFirstByUserIdAndActiveTrueAndApprovedFalseAndOrigin(userId, MembershipOrigin.REQUEST)
                .map(CompanyMembershipPersistenceMapper::toDomain);
    }
}
