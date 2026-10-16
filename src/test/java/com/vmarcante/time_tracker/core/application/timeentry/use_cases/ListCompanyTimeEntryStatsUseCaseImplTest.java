package com.vmarcante.time_tracker.core.application.timeentry.use_cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.CompanyTimeEntriesFilterInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.CompanyTimeEntryStatsOutputDTO;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectFilter;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntryFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

class ListCompanyTimeEntryStatsUseCaseImplTest {

    private TimeEntryRepository timeEntryRepository;
    private TimeEntrySessionRepository sessionRepository;
    private CompanyMembershipRepository membershipRepository;
    private PersonRepository personRepository;
    private ProjectRepository projectRepository;
    private SecurityContextPort securityContext;
    private ListCompanyTimeEntryStatsUseCaseImpl useCase;

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();
    private static final UUID MEMBER_ID = UUID.randomUUID();
    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final UUID ENTRY_ID = UUID.randomUUID();

    private static final LocalDateTime FROM = LocalDateTime.of(2026, 10, 1, 0, 0);
    private static final LocalDateTime TO = LocalDateTime.of(2026, 10, 3, 23, 59);

    @BeforeEach
    void setUp() {
        timeEntryRepository = mock(TimeEntryRepository.class);
        sessionRepository = mock(TimeEntrySessionRepository.class);
        membershipRepository = mock(CompanyMembershipRepository.class);
        personRepository = mock(PersonRepository.class);
        projectRepository = mock(ProjectRepository.class);
        securityContext = mock(SecurityContextPort.class);
        useCase = new ListCompanyTimeEntryStatsUseCaseImpl(timeEntryRepository, sessionRepository,
                membershipRepository, personRepository, projectRepository, securityContext);

        when(securityContext.getCurrentUserId()).thenReturn(Optional.of(ACTOR_ID));
    }

    private CompanyTimeEntriesFilterInputDTO filter() {
        CompanyTimeEntriesFilterInputDTO filter = new CompanyTimeEntriesFilterInputDTO();
        filter.setFrom(FROM);
        filter.setTo(TO);
        return filter;
    }

    private CompanyMembership membership(CompanyRole role) {
        CompanyMembership membership = new CompanyMembership();
        membership.setRole(role);
        membership.setApproved(true);
        return membership;
    }

    private TimeEntry entry(UUID userId, UUID projectId) {
        TimeEntry entry = new TimeEntry();
        entry.setId(ENTRY_ID);
        entry.setUserId(userId);
        entry.setProjectId(projectId);
        entry.setCompanyId(COMPANY_ID);
        return entry;
    }

    private TimeEntrySession session(UUID entryId, LocalDateTime start, LocalDateTime end) {
        TimeEntrySession session = new TimeEntrySession();
        session.setEntryId(entryId);
        session.setStartTime(start);
        session.setEndTime(end);
        return session;
    }

    @Test
    void givenAdminMembership_whenExecuting_thenShouldAggregateStatsPerMemberProjectAndDay() {
        // Arrange
        when(membershipRepository.findOne(any(CompanyMembershipFilter.class)))
                .thenReturn(Optional.of(membership(CompanyRole.OWNER)));

        TimeEntry entry = entry(MEMBER_ID, PROJECT_ID);
        when(timeEntryRepository.search(any(TimeEntryFilter.class)))
                .thenReturn(List.of(entry));

        LocalDateTime start = LocalDateTime.of(2026, 10, 2, 22, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 3, 2, 0);
        when(sessionRepository.findActiveByEntryIds(any()))
                .thenReturn(Map.of(ENTRY_ID, List.of(session(ENTRY_ID, start, end))));

        when(personRepository.findNamesByIds(any())).thenReturn(Map.of(MEMBER_ID, "Ana"));
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setName("Website");
        project.setClientName("Acme");
        when(projectRepository.search(any(ProjectFilter.class))).thenReturn(List.of(project));

        // Act
        CompanyTimeEntryStatsOutputDTO result = useCase.execute(COMPANY_ID, filter());

        // Assert
        assertThat(result.totalSeconds()).isEqualTo(4 * 3600);
        assertThat(result.entryCount()).isEqualTo(1);
        assertThat(result.runningCount()).isZero();
        assertThat(result.memberCount()).isEqualTo(1);

        assertThat(result.days()).hasSize(3);
        assertThat(result.days().get(0).date()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(result.days().get(0).seconds()).isZero();
        assertThat(result.days().get(1).seconds()).isEqualTo(2 * 3600);
        assertThat(result.days().get(2).seconds()).isEqualTo(2 * 3600);

        assertThat(result.members()).hasSize(1);
        assertThat(result.members().get(0).userName()).isEqualTo("Ana");
        assertThat(result.members().get(0).seconds()).isEqualTo(4 * 3600);
        assertThat(result.members().get(0).entryCount()).isEqualTo(1);

        assertThat(result.projects()).hasSize(1);
        assertThat(result.projects().get(0).projectName()).isEqualTo("Website");
        assertThat(result.projects().get(0).clientName()).isEqualTo("Acme");
        assertThat(result.projects().get(0).seconds()).isEqualTo(4 * 3600);
    }

    @Test
    void givenRunningSession_whenExecuting_thenShouldCountRunningAndClampToRangeEnd() {
        // Arrange
        when(membershipRepository.findOne(any(CompanyMembershipFilter.class)))
                .thenReturn(Optional.of(membership(CompanyRole.OWNER)));

        TimeEntry entry = entry(MEMBER_ID, PROJECT_ID);
        when(timeEntryRepository.search(any(TimeEntryFilter.class)))
                .thenReturn(List.of(entry));

        LocalDateTime start = LocalDateTime.of(2026, 10, 3, 20, 0);
        when(sessionRepository.findActiveByEntryIds(any()))
                .thenReturn(Map.of(ENTRY_ID, List.of(session(ENTRY_ID, start, null))));

        when(personRepository.findNamesByIds(any())).thenReturn(Map.of());
        when(projectRepository.search(any(ProjectFilter.class))).thenReturn(List.of());

        // Act
        CompanyTimeEntryStatsOutputDTO result = useCase.execute(COMPANY_ID, filter());

        // Assert
        assertThat(result.runningCount()).isEqualTo(1);
        assertThat(result.totalSeconds()).isEqualTo(3 * 3600 + 59 * 60);
        assertThat(result.days().get(2).seconds()).isEqualTo(3 * 3600 + 59 * 60);
        assertThat(result.running()).hasSize(1);
        assertThat(result.running().get(0).entryId()).isEqualTo(ENTRY_ID);
        assertThat(result.running().get(0).startedAt()).isEqualTo(start);
    }

    @Test
    void givenMemberMembership_whenExecuting_thenShouldUseLeadScope() {
        // Arrange
        when(membershipRepository.findOne(any(CompanyMembershipFilter.class)))
                .thenReturn(Optional.of(membership(CompanyRole.MEMBER)));

        when(timeEntryRepository.search(any(TimeEntryFilter.class)))
                .thenReturn(List.of());
        when(personRepository.findNamesByIds(any())).thenReturn(Map.of());

        // Act
        CompanyTimeEntryStatsOutputDTO result = useCase.execute(COMPANY_ID, filter());

        // Assert
        assertThat(result.totalSeconds()).isZero();
        assertThat(result.entryCount()).isZero();
        assertThat(result.days()).hasSize(3);
        verify(timeEntryRepository, times(2))
                .search(argThat((TimeEntryFilter filter) -> COMPANY_ID.equals(filter.getCompanyId())
                        && ACTOR_ID.equals(filter.getLeadScopeUserId())));
    }

    @Test
    void givenEntryWithoutProject_whenExecuting_thenShouldExcludeFromProjectStats() {
        // Arrange
        when(membershipRepository.findOne(any(CompanyMembershipFilter.class)))
                .thenReturn(Optional.of(membership(CompanyRole.OWNER)));

        TimeEntry entry = entry(MEMBER_ID, null);
        when(timeEntryRepository.search(any(TimeEntryFilter.class)))
                .thenReturn(List.of(entry));

        LocalDateTime start = LocalDateTime.of(2026, 10, 2, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 2, 11, 0);
        when(sessionRepository.findActiveByEntryIds(any()))
                .thenReturn(Map.of(ENTRY_ID, List.of(session(ENTRY_ID, start, end))));
        when(personRepository.findNamesByIds(any())).thenReturn(Map.of(MEMBER_ID, "Ana"));

        // Act
        CompanyTimeEntryStatsOutputDTO result = useCase.execute(COMPANY_ID, filter());

        // Assert
        assertThat(result.totalSeconds()).isEqualTo(3600);
        assertThat(result.projects()).isEmpty();
        assertThat(result.members()).hasSize(1);
    }

    @Test
    void givenUnapprovedMembership_whenExecuting_thenShouldThrowForbidden() {
        // Arrange
        CompanyMembership membership = membership(CompanyRole.OWNER);
        membership.setApproved(false);
        when(membershipRepository.findOne(any(CompanyMembershipFilter.class)))
                .thenReturn(Optional.of(membership));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(COMPANY_ID, filter()))
                .isInstanceOf(ApplicationException.class);
    }
}
