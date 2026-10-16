package com.vmarcante.time_tracker.core.infrastructure.company.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vmarcante.time_tracker.base.infrastructure.persistence.BaseJpaRepository;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;

public interface CompanyMembershipJpaRepository extends BaseJpaRepository<CompanyMembershipJpaEntity, UUID, Integer> {

    Optional<CompanyMembershipJpaEntity> findByUserIdAndCompanyIdAndActiveTrueAndApprovedTrue(
            UUID userId, UUID companyId);

    Optional<CompanyMembershipJpaEntity> findByUserIdAndCompanyIdAndActiveTrueAndApprovedFalse(
            UUID userId, UUID companyId);

    boolean existsByUserIdAndCompanyIdAndActiveTrueAndApprovedTrue(UUID userId, UUID companyId);

    boolean existsByUserIdAndCompanyIdAndActiveTrue(UUID userId, UUID companyId);

    boolean existsByUserIdAndActiveTrueAndApprovedTrue(UUID userId);

    List<CompanyMembershipJpaEntity> findByCompanyIdAndActiveTrueAndApprovedTrue(UUID companyId);

    Page<CompanyMembershipJpaEntity> findByCompanyIdAndActiveTrueAndApprovedTrue(UUID companyId, Pageable pageable);

    List<CompanyMembershipJpaEntity> findByCompanyIdAndActiveTrueAndApprovedFalse(UUID companyId);

    Page<CompanyMembershipJpaEntity> findByCompanyIdAndActiveTrueAndApprovedFalse(UUID companyId, Pageable pageable);

    List<CompanyMembershipJpaEntity> findByUserIdAndActiveTrueAndApprovedFalse(UUID userId);

    Page<CompanyMembershipJpaEntity> findByUserIdAndActiveTrueAndApprovedFalse(UUID userId, Pageable pageable);

    Page<CompanyMembershipJpaEntity> findByUserIdAndActiveTrueAndApprovedFalseAndOrigin(
            UUID userId, MembershipOrigin origin, Pageable pageable);

    long countByUserIdAndActiveTrueAndApprovedFalseAndOrigin(UUID userId, MembershipOrigin origin);

    long countByCompanyIdAndActiveTrueAndApprovedFalse(UUID companyId);

    Optional<CompanyMembershipJpaEntity> findFirstByUserIdAndActiveTrueAndApprovedFalseAndOrigin(
            UUID userId, MembershipOrigin origin);

    @Query("""
            SELECT c FROM CompanyMembershipJpaEntity uc
            JOIN CompanyJpaEntity c ON c.id = uc.companyId
            WHERE uc.userId = :userId AND uc.active = true AND uc.approved = true AND c.active = true
            """)
    Page<CompanyJpaEntity> findActiveCompaniesByUserId(
            @Param("userId") UUID userId, Pageable pageable);

    @Query("""
            SELECT uc.companyId FROM CompanyMembershipJpaEntity uc
            WHERE uc.userId = :userId AND uc.active = true AND uc.approved = true
            """)
    List<UUID> findApprovedCompanyIdsByUserId(@Param("userId") UUID userId);

    Optional<CompanyMembershipJpaEntity> findFirstByUserIdAndActiveTrueAndApprovedTrue(UUID userId);

    @Query("""
            SELECT uc FROM CompanyMembershipJpaEntity uc
            WHERE uc.companyId = :companyId AND (uc.approved = true OR uc.active = false)
            ORDER BY uc.updatedAt DESC
            """)
    Page<CompanyMembershipJpaEntity> findResolvedByCompanyId(
            @Param("companyId") UUID companyId, Pageable pageable);
}
