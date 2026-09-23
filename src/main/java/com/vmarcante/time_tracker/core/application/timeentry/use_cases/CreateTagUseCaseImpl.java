package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.TagInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TagOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.CreateTagUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.policy.TimeEntryValidationPolicy;
import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TagRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreateTagUseCaseImpl implements CreateTagUseCase {

    private final TagRepository tagRepository;
    private final TimeEntryValidationPolicy validationPolicy;
    private final SecurityContextPort securityContext;

    public CreateTagUseCaseImpl(
            TagRepository tagRepository,
            TimeEntryValidationPolicy validationPolicy,
            SecurityContextPort securityContext) {
        this.tagRepository = tagRepository;
        this.validationPolicy = validationPolicy;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public TagOutputDTO execute(TagInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }
        UUID userId = currentUserId.get();

        validationPolicy.validateTag(input.name(), input.color());

        if (tagRepository.existsActiveByUserIdAndNameIgnoreCase(userId, input.name().trim())) {
            throw new ApplicationException("tag.name.already.exists", null);
        }

        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(input.name().trim());
        tag.setColor(input.color());
        tag.setActive(true);
        tag.setCreatedBy(userId);
        tag.setUpdatedBy(userId);

        Tag saved = tagRepository.save(tag);

        log.info("[Create Tag] Tag {} created by user {}", saved.getId(), userId);

        return TagOutputDTO.from(saved);
    }
}
