package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.in.DeactivateTagUseCase;
import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TagRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DeactivateTagUseCaseImpl implements DeactivateTagUseCase {

    private final TagRepository tagRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final SecurityContextPort securityContext;

    public DeactivateTagUseCaseImpl(
            TagRepository tagRepository,
            TimeEntryRepository timeEntryRepository,
            SecurityContextPort securityContext) {
        this.tagRepository = tagRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public void execute(UUID tagId) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }
        UUID userId = currentUserId.get();

        Tag tag = tagRepository.findById(tagId)
                .filter(t -> userId.equals(t.getUserId()))
                .orElseThrow(() -> new ApplicationException("tag.not.found", null));

        timeEntryRepository.deactivateTagLinks(tagId, userId);

        tag.setActive(false);
        tag.setUpdatedBy(userId);
        tagRepository.save(tag);

        log.info("[Deactivate Tag] Tag {} deactivated by user {}", tagId, userId);
    }
}
