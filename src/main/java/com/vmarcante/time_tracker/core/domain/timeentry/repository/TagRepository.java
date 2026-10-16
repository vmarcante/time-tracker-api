package com.vmarcante.time_tracker.core.domain.timeentry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;

import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TagFilter;

public interface TagRepository {

    Tag save(Tag tag);

    Optional<Tag> findOne(TagFilter filter);

    List<Tag> search(TagFilter filter);

    List<Tag> search(TagFilter filter, Sort sort);

    boolean exists(TagFilter filter);
}
