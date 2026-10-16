package com.vmarcante.time_tracker.core.application.timeentry.entry.use_cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.entry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.input.UpdateTimeEntryInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.policy.TimeEntryValidationPolicy;
import com.vmarcante.time_tracker.core.domain.project.enums.ProjectStatus;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectAssignmentFilter;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectAssignmentRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

class UpdateTimeEntryUseCaseImplTest {

    private TimeEntryRepository timeEntryRepository;
    private TimeEntryValidationPolicy validationPolicy;
    private TimeEntryDetailAssembler assembler;
    private SecurityContextPort securityContext;
    private ProjectRepository projectRepository;
    private ProjectAssignmentRepository projectAssignmentRepository;
    private UpdateTimeEntryUseCaseImpl useCase;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ENTRY_ID = UUID.randomUUID();
    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final UUID OLD_PROJECT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        timeEntryRepository = mock(TimeEntryRepository.class);
        validationPolicy = mock(TimeEntryValidationPolicy.class);
        assembler = mock(TimeEntryDetailAssembler.class);
        securityContext = mock(SecurityContextPort.class);
        projectRepository = mock(ProjectRepository.class);
        projectAssignmentRepository = mock(ProjectAssignmentRepository.class);
        useCase = new UpdateTimeEntryUseCaseImpl(timeEntryRepository, validationPolicy, assembler,
                securityContext, projectRepository, projectAssignmentRepository);

        when(securityContext.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        TimeEntry existing = new TimeEntry();
        existing.setId(ENTRY_ID);
        existing.setUserId(USER_ID);
        existing.setProjectId(OLD_PROJECT_ID);
        existing.setCompanyId(UUID.randomUUID());
        when(timeEntryRepository.findById(ENTRY_ID)).thenReturn(Optional.of(existing));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(assembler.toDetail(any(TimeEntry.class))).thenReturn(mock(TimeEntryDetailOutputDTO.class));
    }

    private TimeEntry savedEntry() {
        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(timeEntryRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void givenNoProjectFields_whenUpdating_thenKeepsCurrentProject() {
        // Arrange
        UpdateTimeEntryInputDTO input = new UpdateTimeEntryInputDTO("Task", null, null, null);

        // Act
        useCase.execute(ENTRY_ID, input);

        // Assert
        TimeEntry saved = savedEntry();
        assertThat(saved.getProjectId()).isEqualTo(OLD_PROJECT_ID);
    }

    @Test
    void givenNewProjectId_whenUpdating_thenSwitchesProjectAndCompany() {
        // Arrange
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setUserId(USER_ID);
        project.setCompanyId(null);
        project.setStatus(ProjectStatus.ACTIVE);
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

        UpdateTimeEntryInputDTO input = new UpdateTimeEntryInputDTO("Task", null, PROJECT_ID, null);

        // Act
        useCase.execute(ENTRY_ID, input);

        // Assert
        TimeEntry saved = savedEntry();
        assertThat(saved.getProjectId()).isEqualTo(PROJECT_ID);
        assertThat(saved.getCompanyId()).isNull();
    }

    @Test
    void givenClearProject_whenUpdating_thenRemovesProjectAndCompany() {
        // Arrange
        UpdateTimeEntryInputDTO input = new UpdateTimeEntryInputDTO("Task", null, null, true);

        // Act
        useCase.execute(ENTRY_ID, input);

        // Assert
        TimeEntry saved = savedEntry();
        assertThat(saved.getProjectId()).isNull();
        assertThat(saved.getCompanyId()).isNull();
    }

    @Test
    void givenCompanyProjectWithoutAssignment_whenUpdating_thenThrowsForbidden() {
        // Arrange
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setCompanyId(UUID.randomUUID());
        project.setStatus(ProjectStatus.ACTIVE);
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        when(projectAssignmentRepository.exists(any(ProjectAssignmentFilter.class))).thenReturn(false);

        UpdateTimeEntryInputDTO input = new UpdateTimeEntryInputDTO("Task", null, PROJECT_ID, null);

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(ENTRY_ID, input)).isInstanceOf(ApplicationException.class);
    }
}
