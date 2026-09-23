package com.vmarcante.time_tracker.core.infraestructure.timeentry.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagJpaRepository extends JpaRepository<TagJpaEntity, UUID> {

    Optional<TagJpaEntity> findByIdAndActiveTrue(UUID id);

    List<TagJpaEntity> findByUserIdAndActiveTrueOrderByNameAsc(UUID userId);

    List<TagJpaEntity> findByIdInAndUserIdAndActiveTrue(Collection<UUID> ids, UUID userId);

    boolean existsByUserIdAndNameIgnoreCaseAndActiveTrue(UUID userId, String name);
}
