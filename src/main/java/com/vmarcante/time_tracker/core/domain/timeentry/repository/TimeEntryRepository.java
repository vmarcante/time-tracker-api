package com.vmarcante.time_tracker.core.domain.timeentry.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntryFilter;

public interface TimeEntryRepository {

    TimeEntry save(TimeEntry entry);

    Optional<TimeEntry> findById(UUID entryId);

    boolean existsActiveByIdAndUserId(UUID entryId, UUID userId);

    Page<TimeEntry> search(TimeEntryFilter filter, Pageable pageable);

    List<TimeEntry> search(TimeEntryFilter filter);

    List<UUID> findDistinctProjectIdsByUserId(UUID userId);

    boolean canLeadViewEntry(UUID entryId, UUID leadUserId);

    List<Tag> findActiveTagsByEntryId(UUID entryId);

    List<Tag> findActiveTagsByEntryIds(Collection<UUID> entryIds);

    Map<UUID, List<Tag>> findTagsGroupedByEntryIds(Collection<UUID> entryIds);

    void replaceTags(UUID entryId, Collection<UUID> tagIds, UUID actorId);

    void deactivateTagLinks(UUID tagId, UUID actorId);
}
