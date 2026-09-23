package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.TagInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TagOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.UpdateTagUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.policy.TimeEntryValidationPolicy;
import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TagRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UpdateTagUseCaseImpl implements UpdateTagUseCase {

    private final TagRepository tagRepository;
    private final TimeEntryValidationPolicy validationPolicy;
    private final SecurityContextPort securityContext;

    public UpdateTagUseCaseImpl(
            TagRepository tagRepository,
            TimeEntryValidationPolicy validationPolicy,
            SecurityContextPort securityContext) {
        this.tagRepository = tagRepository;
        this.validationPolicy = validationPolicy;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public TagOutputDTO execute(UUID tagId, TagInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }
        UUID userId = currentUserId.get();

        validationPolicy.validateTag(input.name(), input.color());

        Tag tag = tagRepository.findById(tagId)
                .filter(t -> userId.equals(t.getUserId()))
                .orElseThrow(() -> new ApplicationException("tag.not.found", null));

        if (!tag.getName().equalsIgnoreCase(input.name().trim())
                && tagRepository.existsActiveByUserIdAndNameIgnoreCase(userId, input.name().trim())) {
            throw new ApplicationException("tag.name.already.exists", null);
        }

        tag.setName(input.name().trim());
        tag.setColor(input.color());
        tag.setUpdatedBy(userId);

        Tag saved = tagRepository.save(tag);

        log.info("[Update Tag] Tag {} updated by user {}", tagId, userId);

        return TagOutputDTO.from(saved);
    }
}
