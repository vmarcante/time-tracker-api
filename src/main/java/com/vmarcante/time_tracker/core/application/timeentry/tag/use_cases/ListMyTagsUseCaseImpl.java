package com.vmarcante.time_tracker.core.application.timeentry.tag.use_cases;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TagFilterMapper;

import org.springframework.stereotype.Service;

import org.springframework.data.domain.Sort;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.tag.dto.output.TagOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.tag.in.ListMyTagsUseCase;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TagFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TagRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class ListMyTagsUseCaseImpl implements ListMyTagsUseCase {

    private final TagRepository tagRepository;
    private final SecurityContextPort securityContext;

    public ListMyTagsUseCaseImpl(
            TagRepository tagRepository,
            SecurityContextPort securityContext) {
        this.tagRepository = tagRepository;
        this.securityContext = securityContext;
    }

    @Override
    public List<TagOutputDTO> execute() throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        TagFilter tagFilter = TagFilterMapper.activeByUserId(currentUserId.get());
        return tagRepository.search(tagFilter, Sort.by("name").ascending())
                .stream()
                .map(TagOutputDTO::from)
                .toList();
    }
}
