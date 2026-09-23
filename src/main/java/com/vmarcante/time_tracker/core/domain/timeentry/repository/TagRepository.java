package com.vmarcante.time_tracker.core.domain.timeentry.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;

public interface TagRepository {

    Tag save(Tag tag);

    Optional<Tag> findById(UUID tagId);

    List<Tag> findActiveByUserId(UUID userId);

    List<Tag> findActiveByIdsAndUserId(Collection<UUID> tagIds, UUID userId);

    boolean existsActiveByUserIdAndNameIgnoreCase(UUID userId, String name);
}
