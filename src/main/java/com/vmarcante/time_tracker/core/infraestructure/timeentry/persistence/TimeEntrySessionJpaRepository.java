package com.vmarcante.time_tracker.core.infraestructure.timeentry.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeEntrySessionJpaRepository extends JpaRepository<TimeEntrySessionJpaEntity, UUID> {

    Optional<TimeEntrySessionJpaEntity> findByIdAndActiveTrue(UUID id);

    List<TimeEntrySessionJpaEntity> findByEntryIdAndActiveTrueOrderByStartTimeAsc(UUID entryId);

    List<TimeEntrySessionJpaEntity> findByEntryIdInAndActiveTrueOrderByStartTimeAsc(Collection<UUID> entryIds);

    Optional<TimeEntrySessionJpaEntity> findByUserIdAndEndTimeIsNullAndActiveTrue(UUID userId);

    boolean existsByUserIdAndEndTimeIsNullAndActiveTrue(UUID userId);

    boolean existsByIdAndEntryIdAndActiveTrue(UUID id, UUID entryId);
}
