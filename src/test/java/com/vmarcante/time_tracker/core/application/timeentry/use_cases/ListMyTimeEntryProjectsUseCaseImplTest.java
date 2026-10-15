package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryProjectOutputDTO;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

class ListMyTimeEntryProjectsUseCaseImplTest {

    private TimeEntryRepository timeEntryRepository;
    private ProjectRepository projectRepository;
    private SecurityContextPort securityContext;
    private ListMyTimeEntryProjectsUseCaseImpl useCase;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID PROJECT_A_ID = UUID.randomUUID();
    private static final UUID PROJECT_B_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        timeEntryRepository = mock(TimeEntryRepository.class);
        projectRepository = mock(ProjectRepository.class);
        securityContext = mock(SecurityContextPort.class);
        useCase = new ListMyTimeEntryProjectsUseCaseImpl(timeEntryRepository, projectRepository, securityContext);
    }

    private Project project(UUID id, String name, String clientName) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setClientName(clientName);
        return project;
    }

    @Test
    void givenEntriesInProjects_whenExecuting_thenReturnsDistinctProjectsSortedByName() {
        // Arrange
        when(securityContext.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(timeEntryRepository.findDistinctProjectIdsByUserId(USER_ID))
                .thenReturn(List.of(PROJECT_A_ID, PROJECT_B_ID));
        when(projectRepository.findAllByIds(List.of(PROJECT_A_ID, PROJECT_B_ID)))
                .thenReturn(List.of(
                        project(PROJECT_B_ID, "Zeta", "Cliente Z"),
                        project(PROJECT_A_ID, "alpha", "Cliente A")));

        // Act
        List<TimeEntryProjectOutputDTO> result = useCase.execute();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("alpha");
        assertThat(result.get(0).clientName()).isEqualTo("Cliente A");
        assertThat(result.get(1).name()).isEqualTo("Zeta");
    }

    @Test
    void givenNoEntries_whenExecuting_thenReturnsEmptyWithoutLoadingProjects() {
        // Arrange
        when(securityContext.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(timeEntryRepository.findDistinctProjectIdsByUserId(USER_ID)).thenReturn(List.of());

        // Act
        List<TimeEntryProjectOutputDTO> result = useCase.execute();

        // Assert
        assertThat(result).isEmpty();
        verifyNoInteractions(projectRepository);
    }

    @Test
    void givenUnauthenticated_whenExecuting_thenThrows() {
        // Arrange
        when(securityContext.getCurrentUserId()).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute()).isInstanceOf(ApplicationException.class);
    }
}
