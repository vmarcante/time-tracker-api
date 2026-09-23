package com.vmarcante.time_tracker.core.infraestructure.company.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyJpaRepository extends JpaRepository<CompanyJpaEntity, UUID> {

    Optional<CompanyJpaEntity> findByDocument(String document);

    boolean existsByDocument(String document);
}
