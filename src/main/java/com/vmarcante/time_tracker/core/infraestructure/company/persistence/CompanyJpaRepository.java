package com.vmarcante.time_tracker.core.infraestructure.company.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyJpaRepository extends JpaRepository<CompanyJpaEntity, UUID> {

    Optional<CompanyJpaEntity> findByDocument(String document);

    boolean existsByDocument(String document);

    @Query("""
            SELECT c.id, c.legalName FROM CompanyJpaEntity c
            WHERE c.id IN :ids
            """)
    List<Object[]> findIdAndLegalNameByIds(@Param("ids") Collection<UUID> ids);
}
