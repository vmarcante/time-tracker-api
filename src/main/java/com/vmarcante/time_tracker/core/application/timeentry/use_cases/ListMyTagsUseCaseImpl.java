package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TagOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.ListMyTagsUseCase;
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

        return tagRepository.findActiveByUserId(currentUserId.get())
                .stream()
                .map(TagOutputDTO::from)
                .toList();
    }
}
