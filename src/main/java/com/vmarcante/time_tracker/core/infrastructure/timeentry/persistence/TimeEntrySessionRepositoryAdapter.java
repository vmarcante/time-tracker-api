package com.vmarcante.time_tracker.core.infrastructure.timeentry.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntrySessionFilterMapper;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntrySessionFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;
import com.vmarcante.time_tracker.core.infrastructure.timeentry.mapper.TimeEntryPersistenceMapper;

@Component
public class TimeEntrySessionRepositoryAdapter implements TimeEntrySessionRepository {

    private final TimeEntrySessionJpaRepository sessionJpaRepository;

    public TimeEntrySessionRepositoryAdapter(TimeEntrySessionJpaRepository sessionJpaRepository) {
        this.sessionJpaRepository = sessionJpaRepository;
    }

    @Override
    public TimeEntrySession save(TimeEntrySession session) {
        TimeEntrySessionJpaEntity entity = TimeEntryPersistenceMapper.toEntity(session);
        return TimeEntryPersistenceMapper.toDomain(sessionJpaRepository.save(entity));
    }

    @Override
    public Optional<TimeEntrySession> findOne(TimeEntrySessionFilter filter) {
        return sessionJpaRepository
                .findOne(TimeEntrySessionRepositorySpecification.withFilter(filter))
                .map(TimeEntryPersistenceMapper::toDomain);
    }

    @Override
    public List<TimeEntrySession> search(TimeEntrySessionFilter filter) {
        return sessionJpaRepository
                .findAll(TimeEntrySessionRepositorySpecification.withFilter(filter))
                .stream()
                .map(TimeEntryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<TimeEntrySession> search(TimeEntrySessionFilter filter, Sort sort) {
        return sessionJpaRepository
                .findAll(TimeEntrySessionRepositorySpecification.withFilter(filter), sort)
                .stream()
                .map(TimeEntryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Map<UUID, List<TimeEntrySession>> findActiveByEntryIds(Collection<UUID> entryIds) {
        if (entryIds == null || entryIds.isEmpty()) {
            return Map.of();
        }
        TimeEntrySessionFilter filter = TimeEntrySessionFilterMapper.activeByEntryIds(entryIds);
        return search(filter, Sort.by("startTime").ascending()).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(s -> Objects.requireNonNull(s.getEntryId())));
    }

    @Override
    public boolean exists(TimeEntrySessionFilter filter) {
        return sessionJpaRepository.exists(TimeEntrySessionRepositorySpecification.withFilter(filter));
    }
}
