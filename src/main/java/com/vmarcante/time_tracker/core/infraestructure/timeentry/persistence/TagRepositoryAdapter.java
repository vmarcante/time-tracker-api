package com.vmarcante.time_tracker.core.infraestructure.timeentry.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TagRepository;
import com.vmarcante.time_tracker.core.infraestructure.timeentry.mapper.TimeEntryPersistenceMapper;

@Component
public class TagRepositoryAdapter implements TagRepository {

    private final TagJpaRepository tagJpaRepository;

    public TagRepositoryAdapter(TagJpaRepository tagJpaRepository) {
        this.tagJpaRepository = tagJpaRepository;
    }

    @Override
    public Tag save(Tag tag) {
        TagJpaEntity entity = TimeEntryPersistenceMapper.toEntity(tag);
        return TimeEntryPersistenceMapper.toDomain(tagJpaRepository.save(entity));
    }

    @Override
    public Optional<Tag> findById(UUID tagId) {
        return tagJpaRepository.findByIdAndActiveTrue(tagId)
                .map(TimeEntryPersistenceMapper::toDomain);
    }

    @Override
    public List<Tag> findActiveByUserId(UUID userId) {
        return tagJpaRepository.findByUserIdAndActiveTrueOrderByNameAsc(userId)
                .stream()
                .map(TimeEntryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Tag> findActiveByIdsAndUserId(Collection<UUID> tagIds, UUID userId) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        return tagJpaRepository.findByIdInAndUserIdAndActiveTrue(tagIds, userId)
                .stream()
                .map(TimeEntryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByUserIdAndNameIgnoreCase(UUID userId, String name) {
        return tagJpaRepository.existsByUserIdAndNameIgnoreCaseAndActiveTrue(userId, name);
    }
}
