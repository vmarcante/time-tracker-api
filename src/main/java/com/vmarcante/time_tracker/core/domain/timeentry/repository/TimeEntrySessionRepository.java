package com.vmarcante.time_tracker.core.domain.timeentry.repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;

public interface TimeEntrySessionRepository {

    TimeEntrySession save(TimeEntrySession session);

    Optional<TimeEntrySession> findById(UUID sessionId);

    List<TimeEntrySession> findActiveByEntryId(UUID entryId);

    Map<UUID, List<TimeEntrySession>> findActiveByEntryIds(Collection<UUID> entryIds);

    Optional<TimeEntrySession> findRunningByUserId(UUID userId);

    boolean existsRunningByUserId(UUID userId);

    boolean existsActiveByIdAndEntryId(UUID sessionId, UUID entryId);
}
