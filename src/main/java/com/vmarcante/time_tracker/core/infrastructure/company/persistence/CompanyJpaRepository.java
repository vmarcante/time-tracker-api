package com.vmarcante.time_tracker.core.infrastructure.company.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vmarcante.time_tracker.base.infrastructure.persistence.BaseJpaRepository;

public interface CompanyJpaRepository extends BaseJpaRepository<CompanyJpaEntity, UUID, Integer> {

    @Query("""
            SELECT c.id, c.legalName FROM CompanyJpaEntity c
            WHERE c.id IN :ids
            """)
    List<Object[]> findIdAndLegalNameByIds(@Param("ids") Collection<UUID> ids);
}
