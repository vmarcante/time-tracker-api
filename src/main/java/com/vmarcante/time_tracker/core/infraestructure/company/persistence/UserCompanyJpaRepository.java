package com.vmarcante.time_tracker.core.infraestructure.company.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;

public interface UserCompanyJpaRepository extends JpaRepository<UserCompanyJpaEntity, UUID> {

    Optional<UserCompanyJpaEntity> findByUserIdAndCompanyIdAndActiveTrueAndApprovedTrue(
            UUID userId, UUID companyId);

    Optional<UserCompanyJpaEntity> findByUserIdAndCompanyIdAndActiveTrueAndApprovedFalse(
            UUID userId, UUID companyId);

    boolean existsByUserIdAndCompanyIdAndActiveTrueAndApprovedTrue(UUID userId, UUID companyId);

    boolean existsByUserIdAndCompanyIdAndActiveTrue(UUID userId, UUID companyId);

    boolean existsByUserIdAndActiveTrueAndApprovedTrue(UUID userId);

    List<UserCompanyJpaEntity> findByCompanyIdAndActiveTrueAndApprovedTrue(UUID companyId);

    Page<UserCompanyJpaEntity> findByCompanyIdAndActiveTrueAndApprovedTrue(UUID companyId, Pageable pageable);

    List<UserCompanyJpaEntity> findByCompanyIdAndActiveTrueAndApprovedFalse(UUID companyId);

    Page<UserCompanyJpaEntity> findByCompanyIdAndActiveTrueAndApprovedFalse(UUID companyId, Pageable pageable);

    List<UserCompanyJpaEntity> findByUserIdAndActiveTrueAndApprovedFalse(UUID userId);

    Page<UserCompanyJpaEntity> findByUserIdAndActiveTrueAndApprovedFalse(UUID userId, Pageable pageable);

    Page<UserCompanyJpaEntity> findByUserIdAndActiveTrueAndApprovedFalseAndOrigin(
            UUID userId, MembershipOrigin origin, Pageable pageable);

    long countByUserIdAndActiveTrueAndApprovedFalseAndOrigin(UUID userId, MembershipOrigin origin);

    @Query("""
            SELECT c.legalName FROM UserCompanyJpaEntity uc
            JOIN CompanyJpaEntity c ON c.id = uc.companyId
            WHERE uc.userId = :userId AND uc.active = true AND uc.approved = false
            AND uc.origin = :origin AND c.active = true
            """)
    Optional<String> findPendingCompanyNameByUserIdAndOrigin(
            @Param("userId") UUID userId, @Param("origin") MembershipOrigin origin);

    @Query("""
            SELECT c FROM UserCompanyJpaEntity uc
            JOIN CompanyJpaEntity c ON c.id = uc.companyId
            WHERE uc.userId = :userId AND uc.active = true AND uc.approved = true AND c.active = true
            """)
    Page<CompanyJpaEntity> findActiveCompaniesByUserId(
            @Param("userId") UUID userId, Pageable pageable);

    @Query("""
            SELECT uc.companyId FROM UserCompanyJpaEntity uc
            WHERE uc.userId = :userId AND uc.active = true AND uc.approved = true
            """)
    List<UUID> findApprovedCompanyIdsByUserId(@Param("userId") UUID userId);
}
