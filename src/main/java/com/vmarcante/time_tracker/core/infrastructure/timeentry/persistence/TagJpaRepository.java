package com.vmarcante.time_tracker.core.infrastructure.timeentry.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.vmarcante.time_tracker.base.infrastructure.persistence.BaseJpaRepository;


public interface TagJpaRepository extends BaseJpaRepository<TagJpaEntity, UUID, Integer> {

    Optional<TagJpaEntity> findByIdAndActiveTrue(UUID id);

    List<TagJpaEntity> findByUserIdAndActiveTrueOrderByNameAsc(UUID userId);

    List<TagJpaEntity> findByIdInAndUserIdAndActiveTrue(Collection<UUID> ids, UUID userId);

    boolean existsByUserIdAndNameIgnoreCaseAndActiveTrue(UUID userId, String name);
}
