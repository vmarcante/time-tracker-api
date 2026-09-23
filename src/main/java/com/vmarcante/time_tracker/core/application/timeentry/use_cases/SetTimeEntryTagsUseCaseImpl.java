package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.SetTimeEntryTagsInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.SetTimeEntryTagsUseCase;
import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TagRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class SetTimeEntryTagsUseCaseImpl implements SetTimeEntryTagsUseCase {

    private final TimeEntryRepository timeEntryRepository;
    private final TagRepository tagRepository;
    private final TimeEntryDetailAssembler assembler;
    private final SecurityContextPort securityContext;

    public SetTimeEntryTagsUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            TagRepository tagRepository,
            TimeEntryDetailAssembler assembler,
            SecurityContextPort securityContext) {
        this.timeEntryRepository = timeEntryRepository;
        this.tagRepository = tagRepository;
        this.assembler = assembler;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public TimeEntryDetailOutputDTO execute(UUID entryId, SetTimeEntryTagsInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }
        UUID userId = currentUserId.get();

        TimeEntry entry = timeEntryRepository.findById(entryId)
                .filter(e -> userId.equals(e.getUserId()))
                .orElseThrow(() -> new ApplicationException("timeentry.not.found", null));

        List<UUID> tagIds = input.tagIds() == null ? List.of() : input.tagIds();
        List<Tag> tags = tagRepository.findActiveByIdsAndUserId(tagIds, userId);
        if (tags.size() != tagIds.size()) {
            throw new ApplicationException("timeentry.tag.not.found", null);
        }

        timeEntryRepository.replaceTags(entryId, tagIds, userId);

        return assembler.toDetail(entry);
    }
}
