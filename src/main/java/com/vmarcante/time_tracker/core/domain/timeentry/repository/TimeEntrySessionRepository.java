package com.vmarcante.time_tracker.core.domain.timeentry.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntrySessionFilter;

public interface TimeEntrySessionRepository {

    TimeEntrySession save(TimeEntrySession session);

    Optional<TimeEntrySession> findOne(TimeEntrySessionFilter filter);

    List<TimeEntrySession> search(TimeEntrySessionFilter filter);

    List<TimeEntrySession> search(TimeEntrySessionFilter filter, Sort sort);

    Map<UUID, List<TimeEntrySession>> findActiveByEntryIds(Collection<UUID> entryIds);

    boolean exists(TimeEntrySessionFilter filter);
}
