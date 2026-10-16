package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.assembler.TimeEntryDetailAssembler;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.MyTimeEntriesFilterInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntrySummaryOutputDTO;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntryFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

class ListMyTimeEntriesUseCaseImplTest {

    private TimeEntryRepository timeEntryRepository;
    private SecurityContextPort securityContext;
    private ListMyTimeEntriesUseCaseImpl useCase;

    private static final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        timeEntryRepository = mock(TimeEntryRepository.class);
        TimeEntrySessionRepository sessionRepository = mock(TimeEntrySessionRepository.class);
        ProjectRepository projectRepository = mock(ProjectRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        securityContext = mock(SecurityContextPort.class);
        TimeEntryDetailAssembler assembler = new TimeEntryDetailAssembler(
                sessionRepository, timeEntryRepository, projectRepository, personRepository);
        useCase = new ListMyTimeEntriesUseCaseImpl(timeEntryRepository, assembler, securityContext);
    }

    @Test
    void givenNameFilter_whenExecuting_thenPassesNameToRepository() throws ApplicationException {
        // Arrange
        Page<TimeEntry> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(securityContext.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(timeEntryRepository.search(any(TimeEntryFilter.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        MyTimeEntriesFilterInputDTO input = new MyTimeEntriesFilterInputDTO();
        input.setName("fix");

        // Act
        PageWrapperDTO<TimeEntrySummaryOutputDTO> result = useCase.execute(input);

        // Assert
        assertThat(result.content()).isEmpty();
        assertThat(result.pageNumber()).isEqualTo(1);
        assertThat(result.totalElements()).isZero();
        verify(timeEntryRepository).search(argThat((TimeEntryFilter filter) -> USER_ID
                        .equals(filter.getUserId()) && "fix".equals(filter.getName())),
                any(Pageable.class));
    }

    @Test
    void givenNoFilters_whenExecuting_thenSearchesWithinUserScopeOnly() throws ApplicationException {
        // Arrange
        Page<TimeEntry> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(securityContext.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(timeEntryRepository.search(any(TimeEntryFilter.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        PageWrapperDTO<TimeEntrySummaryOutputDTO> result = useCase.execute(new MyTimeEntriesFilterInputDTO());

        // Assert
        assertThat(result.content()).isEmpty();
        verify(timeEntryRepository).search(argThat((TimeEntryFilter filter) -> USER_ID
                        .equals(filter.getUserId()) && filter.getName() == null
                        && filter.getProjectId() == null && filter.getTagIds() == null
                        && filter.getFrom() == null && filter.getTo() == null),
                any(Pageable.class));
    }

    @Test
    void givenUnauthenticatedUser_whenExecuting_thenThrows() {
        // Arrange
        when(securityContext.getCurrentUserId()).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> useCase.execute(new MyTimeEntriesFilterInputDTO()))
                .isInstanceOf(ApplicationException.class);
        verifyNoInteractions(timeEntryRepository);
    }
}
