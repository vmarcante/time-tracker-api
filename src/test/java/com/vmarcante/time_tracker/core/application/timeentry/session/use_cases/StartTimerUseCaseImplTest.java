package com.vmarcante.time_tracker.core.application.timeentry.session.use_cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.vmarcante.time_tracker.core.application.timeentry.entry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.session.dto.input.StartTimerInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.session.policy.TimeEntrySessionValidationPolicy;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TagFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntrySessionFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TagRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

class StartTimerUseCaseImplTest {

    private TimeEntryRepository timeEntryRepository;
    private TimeEntrySessionRepository sessionRepository;
    private ProjectRepository projectRepository;
    private ProjectAssignmentRepository projectAssignmentRepository;
    private TagRepository tagRepository;
    private TimeEntryDetailAssembler assembler;
    private TimeEntrySessionValidationPolicy validationPolicy;
    private SecurityContextPort securityContext;
    private StartTimerUseCaseImpl useCase;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final UUID TAG_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        timeEntryRepository = mock(TimeEntryRepository.class);
        sessionRepository = mock(TimeEntrySessionRepository.class);
        projectRepository = mock(ProjectRepository.class);
        projectAssignmentRepository = mock(ProjectAssignmentRepository.class);
        tagRepository = mock(TagRepository.class);
        assembler = mock(TimeEntryDetailAssembler.class);
        validationPolicy = new TimeEntrySessionValidationPolicy();
        securityContext = mock(SecurityContextPort.class);
        useCase = new StartTimerUseCaseImpl(timeEntryRepository, sessionRepository, projectRepository,
                projectAssignmentRepository, tagRepository, assembler, validationPolicy, securityContext);

        when(securityContext.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(sessionRepository.exists(any(TimeEntrySessionFilter.class))).thenReturn(false);
        when(sessionRepository.search(any(TimeEntrySessionFilter.class))).thenReturn(List.of());
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(assembler.toDetail(any(TimeEntry.class))).thenReturn(mock(TimeEntryDetailOutputDTO.class));
    }

    @Test
    void givenNoProjectId_whenStartingTimer_thenCreatesEntryWithoutProject() {
        // Arrange
        StartTimerInputDTO input = new StartTimerInputDTO(null, null, "My task", null, null);

        // Act
        useCase.execute(input);

        // Assert
        verifyNoInteractions(projectRepository, projectAssignmentRepository);

        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(timeEntryRepository).save(captor.capture());
        TimeEntry saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getProjectId()).isNull();
        assertThat(saved.getCompanyId()).isNull();
        assertThat(saved.getName()).isEqualTo("My task");
    }

    @Test
    void givenPersonalProjectId_whenStartingTimer_thenCreatesEntryWithProject() {
        // Arrange
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setUserId(USER_ID);
        project.setCompanyId(null);
        project.setStatus(ProjectStatus.ACTIVE);
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

        StartTimerInputDTO input = new StartTimerInputDTO(null, PROJECT_ID, "My task", null, null);

        // Act
        useCase.execute(input);

        // Assert
        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(timeEntryRepository).save(captor.capture());
        TimeEntry saved = captor.getValue();
        assertThat(saved.getProjectId()).isEqualTo(PROJECT_ID);
        assertThat(saved.getCompanyId()).isNull();
    }

    @Test
    void givenTagIds_whenStartingTimer_thenAttachesTagsToQuickEntry() {
        // Arrange
        Tag tag = new Tag();
        tag.setId(TAG_ID);
        tag.setUserId(USER_ID);
        when(tagRepository.search(any(TagFilter.class)))
                .thenReturn(List.of(tag));

        StartTimerInputDTO input = new StartTimerInputDTO(null, null, "My task", null, List.of(TAG_ID));

        // Act
        useCase.execute(input);

        // Assert
        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(timeEntryRepository).save(captor.capture());
        verify(timeEntryRepository).replaceTags(captor.getValue().getId(), List.of(TAG_ID), USER_ID);
    }

    @Test
    void givenUnknownTagId_whenStartingTimer_thenThrowsNotFound() {
        // Arrange
        when(tagRepository.search(any(TagFilter.class)))
                .thenReturn(List.of());

        StartTimerInputDTO input = new StartTimerInputDTO(null, null, "My task", null, List.of(TAG_ID));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("timeentry.tag.not.found");
    }

    @Test
    void givenThreeParallelRunningNow_whenStartingTimer_thenThrowsMaxParallel() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        TimeEntrySession s1 = sessionStarting(now.minusHours(1));
        TimeEntrySession s2 = sessionStarting(now.minusMinutes(30));
        TimeEntrySession s3 = sessionStarting(now.minusMinutes(10));
        when(sessionRepository.search(any(TimeEntrySessionFilter.class)))
                .thenReturn(List.of(s1, s2, s3));

        StartTimerInputDTO input = new StartTimerInputDTO(null, null, "My task", null, null);

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("timeentry.session.max.parallel");
    }

    private TimeEntrySession sessionStarting(LocalDateTime startTime) {
        TimeEntrySession session = new TimeEntrySession();
        session.setStartTime(startTime);
        return session;
    }
}
