package com.vmarcante.time_tracker.core.infraestructure.company.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.infraestructure.company.mapper.CompanyPersistenceMapper;

@Component
public class CompanyMembershipRepositoryAdapter implements CompanyMembershipRepository {

    private final UserCompanyJpaRepository userCompanyJpaRepository;

    public CompanyMembershipRepositoryAdapter(UserCompanyJpaRepository userCompanyJpaRepository) {
        this.userCompanyJpaRepository = userCompanyJpaRepository;
    }

    @Override
    public CompanyMembership saveMembership(CompanyMembership membership) {
        UserCompanyJpaEntity entity = CompanyPersistenceMapper.toEntity(membership);
        return CompanyPersistenceMapper.toDomain(userCompanyJpaRepository.save(entity));
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
    public Page<CompanyMembership> findResolvedMembershipsByCompanyId(UUID companyId, Pageable pageable) {
        return userCompanyJpaRepository
                .findResolvedByCompanyId(companyId, pageable)
                .map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public Page<CompanyMembership> findPendingInvitationsByUserId(UUID userId, Pageable pageable) {
        return userCompanyJpaRepository
                .findByUserIdAndActiveTrueAndApprovedFalseAndOrigin(userId, MembershipOrigin.INVITE, pageable)
                .map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public Optional<CompanyMembership> findApprovedMembershipByUserId(UUID userId) {
        return userCompanyJpaRepository
                .findFirstByUserIdAndActiveTrueAndApprovedTrue(userId)
                .map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public long countPendingInvitationsByUserId(UUID userId) {
        return userCompanyJpaRepository
                .countByUserIdAndActiveTrueAndApprovedFalseAndOrigin(userId, MembershipOrigin.INVITE);
    }

    @Override
    public long countPendingMembershipsByCompanyId(UUID companyId) {
        return userCompanyJpaRepository
                .countByCompanyIdAndActiveTrueAndApprovedFalse(companyId);
    }

    @Override
    public Optional<String> findPendingRequestCompanyNameByUserId(UUID userId) {
        return userCompanyJpaRepository
                .findPendingCompanyNameByUserIdAndOrigin(userId, MembershipOrigin.REQUEST);
    }
}
