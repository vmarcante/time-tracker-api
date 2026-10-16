package com.vmarcante.time_tracker.core.infrastructure.company.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vmarcante.time_tracker.base.infrastructure.persistence.BaseJpaRepository;

public interface CompanyMembershipJpaRepository extends BaseJpaRepository<CompanyMembershipJpaEntity, UUID, Integer> {

    @Query("""
            SELECT uc.companyId FROM CompanyMembershipJpaEntity uc
            WHERE uc.userId = :userId AND uc.active = true AND uc.approved = true
            """)
    List<UUID> findApprovedCompanyIdsByUserId(@Param("userId") UUID userId);
}
