package com.vmarcante.time_tracker.core.infrastructure.timeentry.persistence;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntryTagFilterMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntryFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntryTagFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.infrastructure.timeentry.mapper.TimeEntryPersistenceMapper;

@Component
public class TimeEntryRepositoryAdapter implements TimeEntryRepository {

    private final TimeEntryJpaRepository timeEntryJpaRepository;
    private final TimeEntryTagJpaRepository timeEntryTagJpaRepository;

    public TimeEntryRepositoryAdapter(
            TimeEntryJpaRepository timeEntryJpaRepository,
            TimeEntryTagJpaRepository timeEntryTagJpaRepository) {
        this.timeEntryJpaRepository = timeEntryJpaRepository;
        this.timeEntryTagJpaRepository = timeEntryTagJpaRepository;
    }

    @Override
    public TimeEntry save(TimeEntry entry) {
        TimeEntryJpaEntity entity = TimeEntryPersistenceMapper.toEntity(entry);
        return TimeEntryPersistenceMapper.toDomain(timeEntryJpaRepository.save(entity));
    }

    @Override
    public Optional<TimeEntry> findById(UUID entryId) {
        return timeEntryJpaRepository.findById(entryId)
                .map(TimeEntryPersistenceMapper::toDomain);
    }

    @Override
    public Optional<TimeEntry> findOne(TimeEntryFilter filter) {
        return timeEntryJpaRepository.findOne(TimeEntryRepositorySpecification.withFilter(filter))
                .map(TimeEntryPersistenceMapper::toDomain);
    }

    @Override
    public Page<TimeEntry> search(TimeEntryFilter filter, Pageable pageable) {
        Specification<TimeEntryJpaEntity> specification = TimeEntryRepositorySpecification.withFilter(filter);
        if (pageable.getSort().isUnsorted()) {
            specification = specification.and(TimeEntryRepositorySpecification.recentFirst());
        }
        return timeEntryJpaRepository.findAll(specification, pageable)
                .map(TimeEntryPersistenceMapper::toDomain);
    }

    @Override
    public List<TimeEntry> search(TimeEntryFilter filter) {
        Specification<TimeEntryJpaEntity> specification = TimeEntryRepositorySpecification.withFilter(filter)
                .and(TimeEntryRepositorySpecification.recentFirst());
        return timeEntryJpaRepository.findAll(specification)
                .stream().map(TimeEntryPersistenceMapper::toDomain).toList();
    }

    @Override
    public List<UUID> findDistinctProjectIdsByUserId(UUID userId) {
        return timeEntryJpaRepository.findDistinctProjectIdsByUserId(userId);
    }

    @Override
    public boolean canLeadViewEntry(UUID entryId, UUID leadUserId) {
        return timeEntryJpaRepository.canLeadViewEntry(entryId, leadUserId);
    }

    @Override
    public List<Tag> findActiveTagsByEntryId(UUID entryId) {
        return findActiveTagsByEntryIds(List.of(entryId));
    }

    @Override
    public List<Tag> findActiveTagsByEntryIds(Collection<UUID> entryIds) {
        if (entryIds == null || entryIds.isEmpty()) {
            return List.of();
        }
        return timeEntryTagJpaRepository.findEntryIdAndTagByEntryIds(entryIds)
                .stream()
                .map(row -> TimeEntryPersistenceMapper.toDomain((TagJpaEntity) row[1]))
                .toList();
    }

    @Override
    public Map<UUID, List<Tag>> findTagsGroupedByEntryIds(Collection<UUID> entryIds) {
        if (entryIds == null || entryIds.isEmpty()) {
            return Map.of();
        }
        return timeEntryTagJpaRepository.findEntryIdAndTagByEntryIds(entryIds)
                .stream()
                .collect(Collectors.groupingBy(
                        row -> (UUID) row[0],
                        Collectors.mapping(row -> TimeEntryPersistenceMapper.toDomain((TagJpaEntity) row[1]),
                                Collectors.toList())));
    }

    @Override
    public void replaceTags(UUID entryId, Collection<UUID> tagIds, UUID actorId) {
        TimeEntryTagFilter linkFilter = TimeEntryTagFilterMapper.activeByEntryId(entryId);
        List<TimeEntryTagJpaEntity> currentLinks = timeEntryTagJpaRepository
                .findAll(TimeEntryTagRepositorySpecification.withFilter(linkFilter));
        Set<UUID> desired = tagIds == null ? Set.of() : new HashSet<>(tagIds);
        Set<UUID> current = currentLinks.stream()
                .filter(Objects::nonNull)
                .map(TimeEntryTagJpaEntity::getTagId)
                .collect(Collectors.toSet());

        currentLinks.stream()
                .filter(link -> !desired.contains(link.getTagId()))
                .forEach(link -> {
                    link.setActive(false);
                    link.setUpdatedBy(actorId);
                    timeEntryTagJpaRepository.save(link);
                });

        desired.stream()
                .filter(tagId -> !current.contains(tagId))
                .forEach(tagId -> timeEntryTagJpaRepository.save(
                        TimeEntryPersistenceMapper.toLinkEntity(entryId, tagId, actorId)));
    }

    @Override
    public void deactivateTagLinks(UUID tagId, UUID actorId) {
        TimeEntryTagFilter linkFilter = TimeEntryTagFilterMapper.activeByTagId(tagId);
        timeEntryTagJpaRepository.findAll(TimeEntryTagRepositorySpecification.withFilter(linkFilter))
                .forEach(link -> {
                    link.setActive(false);
                    link.setUpdatedBy(actorId);
                    timeEntryTagJpaRepository.save(link);
                });
    }
}
