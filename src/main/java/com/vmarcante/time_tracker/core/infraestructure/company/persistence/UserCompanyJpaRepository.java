package com.vmarcante.time_tracker.core.infraestructure.company.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCompanyJpaRepository extends JpaRepository<UserCompanyJpaEntity, UUID> {

    Optional<UserCompanyJpaEntity> findByUserIdAndCompanyIdAndActiveTrueAndApprovedTrue(
            UUID userId, UUID companyId);

    Optional<UserCompanyJpaEntity> findByUserIdAndCompanyIdAndActiveTrueAndApprovedFalse(
            UUID userId, UUID companyId);

    boolean existsByUserIdAndCompanyIdAndActiveTrueAndApprovedTrue(UUID userId, UUID companyId);

    boolean existsByUserIdAndCompanyIdAndActiveTrue(UUID userId, UUID companyId);

    List<UserCompanyJpaEntity> findByCompanyIdAndActiveTrueAndApprovedTrue(UUID companyId);

    List<UserCompanyJpaEntity> findByCompanyIdAndActiveTrueAndApprovedFalse(UUID companyId);

    List<UserCompanyJpaEntity> findByUserIdAndActiveTrueAndApprovedFalse(UUID userId);

    @Query("""
            SELECT uc.companyId FROM UserCompanyJpaEntity uc
            WHERE uc.userId = :userId AND uc.active = true AND uc.approved = true
            """)
    List<UUID> findApprovedCompanyIdsByUserId(@Param("userId") UUID userId);
}
