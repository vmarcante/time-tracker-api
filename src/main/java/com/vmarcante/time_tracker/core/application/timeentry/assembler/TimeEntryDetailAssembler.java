package com.vmarcante.time_tracker.core.application.timeentry.assembler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntrySummaryOutputDTO;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;

@Component
public class TimeEntryDetailAssembler {

    private final TimeEntrySessionRepository sessionRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final ProjectRepository projectRepository;
    private final PersonRepository personRepository;

    public TimeEntryDetailAssembler(
            TimeEntrySessionRepository sessionRepository,
            TimeEntryRepository timeEntryRepository,
            ProjectRepository projectRepository,
            PersonRepository personRepository) {
        this.sessionRepository = sessionRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.projectRepository = projectRepository;
        this.personRepository = personRepository;
    }

    public TimeEntryDetailOutputDTO toDetail(TimeEntry entry) {
        List<TimeEntrySession> sessions = sessionRepository.findActiveByEntryId(entry.getId());
        List<Tag> tags = timeEntryRepository.findActiveTagsByEntryId(entry.getId());
        Project project = projectRepository.findAllByIds(List.of(entry.getProjectId()))
                .stream().findFirst().orElse(null);
        String userName = personRepository.findNameById(entry.getUserId()).orElse(null);
        return TimeEntryDetailOutputDTO.from(entry, sessions, tags, project, userName);
    }

    public Page<TimeEntrySummaryOutputDTO> toSummaryPage(Page<TimeEntry> page) {
        List<TimeEntry> content = page.getContent();
        if (content.isEmpty()) {
            return new PageImpl<>(List.of(), page.getPageable(), page.getTotalElements());
        }

        Set<UUID> entryIds = content.stream().map(TimeEntry::getId).collect(Collectors.toSet());
        Set<UUID> projectIds = content.stream().map(TimeEntry::getProjectId).collect(Collectors.toSet());
        Set<UUID> userIds = content.stream().map(TimeEntry::getUserId).collect(Collectors.toSet());

        Map<UUID, List<TimeEntrySession>> sessionsByEntry = sessionRepository.findActiveByEntryIds(entryIds);
        Map<UUID, List<Tag>> tagsByEntry = timeEntryRepository.findTagsGroupedByEntryIds(entryIds);
        Map<UUID, Project> projectsById = projectRepository.findAllByIds(projectIds)
                .stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));
        Map<UUID, String> namesByUserId = personRepository.findNamesByIds(userIds);

        return page.map(entry -> TimeEntrySummaryOutputDTO.from(
                entry,
                sessionsByEntry.getOrDefault(entry.getId(), List.of()),
                tagsByEntry.getOrDefault(entry.getId(), List.of()),
                projectsById.get(entry.getProjectId()),
                namesByUserId.get(entry.getUserId())));
    }
}
