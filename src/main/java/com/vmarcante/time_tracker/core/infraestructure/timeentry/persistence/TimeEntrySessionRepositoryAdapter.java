package com.vmarcante.time_tracker.core.infraestructure.timeentry.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;
import com.vmarcante.time_tracker.core.infraestructure.timeentry.mapper.TimeEntryPersistenceMapper;

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
    public Optional<TimeEntrySession> findById(UUID sessionId) {
        return sessionJpaRepository.findByIdAndActiveTrue(sessionId)
                .map(TimeEntryPersistenceMapper::toDomain);
    }

    @Override
    public List<TimeEntrySession> findActiveByEntryId(UUID entryId) {
        return sessionJpaRepository.findByEntryIdAndActiveTrueOrderByStartTimeAsc(entryId)
                .stream()
                .map(TimeEntryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Map<UUID, List<TimeEntrySession>> findActiveByEntryIds(Collection<UUID> entryIds) {
        if (entryIds == null || entryIds.isEmpty()) {
            return Map.of();
        }
        return sessionJpaRepository.findByEntryIdInAndActiveTrueOrderByStartTimeAsc(entryIds)
                .stream()
                .map(TimeEntryPersistenceMapper::toDomain)
                .collect(Collectors.groupingBy(TimeEntrySession::getEntryId));
    }

    @Override
    public Optional<TimeEntrySession> findRunningByUserId(UUID userId) {
        return sessionJpaRepository.findByUserIdAndEndTimeIsNullAndActiveTrue(userId)
                .map(TimeEntryPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsRunningByUserId(UUID userId) {
        return sessionJpaRepository.existsByUserIdAndEndTimeIsNullAndActiveTrue(userId);
    }

    @Override
    public boolean existsActiveByIdAndEntryId(UUID sessionId, UUID entryId) {
        return sessionJpaRepository.existsByIdAndEntryIdAndActiveTrue(sessionId, entryId);
    }
}
