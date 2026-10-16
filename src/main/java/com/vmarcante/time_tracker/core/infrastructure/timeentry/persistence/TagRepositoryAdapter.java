package com.vmarcante.time_tracker.core.infrastructure.timeentry.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TagFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TagRepository;
import com.vmarcante.time_tracker.core.infrastructure.timeentry.mapper.TimeEntryPersistenceMapper;

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
    public Optional<Tag> findOne(TagFilter filter) {
        return tagJpaRepository.findOne(TagRepositorySpecification.withFilter(filter))
                .map(TimeEntryPersistenceMapper::toDomain);
    }

    @Override
    public List<Tag> search(TagFilter filter) {
        return tagJpaRepository.findAll(TagRepositorySpecification.withFilter(filter))
                .stream()
                .map(TimeEntryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Tag> search(TagFilter filter, Sort sort) {
        return tagJpaRepository.findAll(TagRepositorySpecification.withFilter(filter), sort)
                .stream()
                .map(TimeEntryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean exists(TagFilter filter) {
        return tagJpaRepository.exists(TagRepositorySpecification.withFilter(filter));
    }
}
