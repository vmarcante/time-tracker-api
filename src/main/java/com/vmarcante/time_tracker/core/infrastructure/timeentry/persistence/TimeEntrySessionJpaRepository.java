package com.vmarcante.time_tracker.core.infrastructure.timeentry.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.vmarcante.time_tracker.base.infrastructure.persistence.BaseJpaRepository;


public interface TimeEntrySessionJpaRepository extends BaseJpaRepository<TimeEntrySessionJpaEntity, UUID, Integer> {

    Optional<TimeEntrySessionJpaEntity> findByIdAndActiveTrue(UUID id);

    List<TimeEntrySessionJpaEntity> findByEntryIdAndActiveTrueOrderByStartTimeAsc(UUID entryId);

    List<TimeEntrySessionJpaEntity> findByEntryIdInAndActiveTrueOrderByStartTimeAsc(Collection<UUID> entryIds);

    Optional<TimeEntrySessionJpaEntity> findByUserIdAndEndTimeIsNullAndActiveTrue(UUID userId);

    boolean existsByUserIdAndEndTimeIsNullAndActiveTrue(UUID userId);

    boolean existsByIdAndEntryIdAndActiveTrue(UUID id, UUID entryId);
}
