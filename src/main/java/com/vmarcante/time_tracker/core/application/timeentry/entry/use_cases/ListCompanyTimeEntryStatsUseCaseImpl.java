package com.vmarcante.time_tracker.core.application.timeentry.entry.use_cases;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilterMapper;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyMembershipFilter;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.input.CompanyTimeEntriesFilterInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.CompanyDayStatsOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.CompanyMemberStatsOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.CompanyProjectDayStatsOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.CompanyProjectStatsOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.CompanyRunningMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.CompanyTimeEntryStatsOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.in.ListCompanyTimeEntryStatsUseCase;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntryFilter;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntryRepository;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectFilter;

@Service
public class ListCompanyTimeEntryStatsUseCaseImpl implements ListCompanyTimeEntryStatsUseCase {

    private static final int DEFAULT_RANGE_DAYS = 29;

    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntrySessionRepository sessionRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final PersonRepository personRepository;
    private final ProjectRepository projectRepository;
    private final SecurityContextPort securityContext;

    public ListCompanyTimeEntryStatsUseCaseImpl(
            TimeEntryRepository timeEntryRepository,
            TimeEntrySessionRepository sessionRepository,
            CompanyMembershipRepository membershipRepository,
            PersonRepository personRepository,
            ProjectRepository projectRepository,
            SecurityContextPort securityContext) {
        this.timeEntryRepository = timeEntryRepository;
        this.sessionRepository = sessionRepository;
        this.membershipRepository = membershipRepository;
        this.personRepository = personRepository;
        this.projectRepository = projectRepository;
        this.securityContext = securityContext;
    }

    @Override
    public CompanyTimeEntryStatsOutputDTO execute(UUID companyId, CompanyTimeEntriesFilterInputDTO input)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }
        UUID actorId = currentUserId.get();

        CompanyMembershipFilter companyMembershipFilter = CompanyMembershipFilterMapper.approvedMembership(actorId, companyId);
        CompanyMembership membership = membershipRepository.findOne(companyMembershipFilter)
                .filter(m -> Boolean.TRUE.equals(m.getApproved()))
                .orElseThrow(() -> new ApplicationException("company.access.denied", null, HttpStatus.FORBIDDEN));

        LocalDateTime toEffective = input.getTo() != null ? input.getTo() : LocalDateTime.now();
        LocalDateTime fromEffective = input.getFrom() != null ? input.getFrom()
                : toEffective.toLocalDate().minusDays(DEFAULT_RANGE_DAYS).atTime(LocalTime.MIN);

        boolean fullScope = membership.getRole().canManage(CompanyRole.MEMBER);
        List<TimeEntry> entries = fetchEntries(companyId, actorId, input,
                fromEffective, toEffective, fullScope);

        Map<UUID, TimeEntry> entryById = entries.stream()
                .collect(Collectors.toMap(TimeEntry::getId, e -> e, (a, b) -> a));

        List<TimeEntrySession> sessions = entryById.isEmpty()
                ? List.of()
                : sessionRepository.findActiveByEntryIds(entryById.keySet()).values().stream()
                        .flatMap(List::stream)
                        .toList();

        Map<LocalDate, Long> secondsByDay = zeroFillDays(fromEffective.toLocalDate(), toEffective.toLocalDate());
        Map<UUID, Long> secondsByUser = new HashMap<>();
        Map<UUID, Long> secondsByProject = new HashMap<>();
        Map<UUID, Map<LocalDate, Long>> secondsByProjectDay = new HashMap<>();
        List<TimeEntrySession> runningSessions = new ArrayList<>();
        long totalSeconds = 0;

        for (TimeEntrySession session : sessions) {
            TimeEntry entry = entryById.get(session.getEntryId());
            if (entry == null) {
                continue;
            }
            LocalDateTime chunkStart = session.getStartTime() != null && session.getStartTime().isAfter(fromEffective)
                    ? session.getStartTime()
                    : fromEffective;
            LocalDateTime chunkEnd = session.getEndTime() != null
                    ? (session.getEndTime().isBefore(toEffective) ? session.getEndTime() : toEffective)
                    : toEffective;
            if (session.getEndTime() == null) {
                runningSessions.add(session);
            }
            while (chunkStart.isBefore(chunkEnd)) {
                LocalDateTime nextDay = chunkStart.toLocalDate().plusDays(1).atTime(LocalTime.MIN);
                LocalDateTime chunkLimit = nextDay.isBefore(chunkEnd) ? nextDay : chunkEnd;
                long chunkSeconds = Duration.between(chunkStart, chunkLimit).getSeconds();
                if (chunkSeconds > 0) {
                    LocalDate day = chunkStart.toLocalDate();
                    secondsByDay.merge(day, chunkSeconds, (a, b) -> a + b);
                    secondsByUser.merge(entry.getUserId(), chunkSeconds, (a, b) -> a + b);
                    if (entry.getProjectId() != null) {
                        secondsByProject.merge(entry.getProjectId(), chunkSeconds, (a, b) -> a + b);
                        secondsByProjectDay
                                .computeIfAbsent(entry.getProjectId(), k -> new HashMap<>())
                                .merge(day, chunkSeconds, (a, b) -> a + b);
                    }
                    totalSeconds += chunkSeconds;
                }
                chunkStart = chunkLimit;
            }
        }

        long rangeDays = ChronoUnit.DAYS.between(fromEffective.toLocalDate(), toEffective.toLocalDate()) + 1;
        LocalDateTime previousTo = fromEffective;
        LocalDateTime previousFrom = fromEffective.minusDays(rangeDays);
        List<TimeEntry> previousEntries = fetchEntries(companyId, actorId, input,
                previousFrom, previousTo, fullScope);
        Map<UUID, TimeEntry> previousEntryById = previousEntries.stream()
                .collect(Collectors.toMap(TimeEntry::getId, e -> e, (a, b) -> a));
        List<TimeEntrySession> previousSessions = previousEntryById.isEmpty()
                ? List.of()
                : sessionRepository.findActiveByEntryIds(previousEntryById.keySet()).values().stream()
                        .flatMap(List::stream)
                        .toList();
        long previousTotalSeconds = sumClampedSeconds(previousSessions, previousFrom, previousTo);

        Map<UUID, String> userNames = personRepository.findNamesByIds(secondsByUser.keySet());
        Map<UUID, Project> projectsById = secondsByProject.isEmpty()
                ? Map.of()
                : searchProjectsByIds(secondsByProject.keySet()).stream()
                        .collect(Collectors.toMap(Project::getId, p -> p, (a, b) -> a));

        Map<UUID, Integer> entryCountByUser = new HashMap<>();
        Map<UUID, Integer> entryCountByProject = new HashMap<>();
        for (TimeEntry entry : entries) {
            entryCountByUser.merge(entry.getUserId(), 1, (a, b) -> a + b);
            if (entry.getProjectId() != null) {
                entryCountByProject.merge(entry.getProjectId(), 1, (a, b) -> a + b);
            }
        }

        List<CompanyMemberStatsOutputDTO> members = secondsByUser.entrySet().stream()
                .map(e -> new CompanyMemberStatsOutputDTO(
                        e.getKey(),
                        userNames.getOrDefault(e.getKey(), ""),
                        e.getValue(),
                        entryCountByUser.getOrDefault(e.getKey(), 0)))
                .sorted(Comparator.comparingLong(CompanyMemberStatsOutputDTO::seconds).reversed())
                .toList();

        List<CompanyProjectStatsOutputDTO> projects = secondsByProject.entrySet().stream()
                .map(e -> {
                    Project project = projectsById.get(e.getKey());
                    return new CompanyProjectStatsOutputDTO(
                            e.getKey(),
                            project != null ? project.getName() : "",
                            project != null ? project.getClientName() : null,
                            e.getValue(),
                            entryCountByProject.getOrDefault(e.getKey(), 0));
                })
                .sorted(Comparator.comparingLong(CompanyProjectStatsOutputDTO::seconds).reversed())
                .toList();

        List<CompanyDayStatsOutputDTO> days = secondsByDay.entrySet().stream()
                .map(e -> new CompanyDayStatsOutputDTO(e.getKey(), e.getValue()))
                .toList();

        List<CompanyProjectDayStatsOutputDTO> projectDays = secondsByProjectDay.entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream()
                        .map(d -> new CompanyProjectDayStatsOutputDTO(e.getKey(), d.getKey(), d.getValue())))
                .sorted(Comparator.comparing(CompanyProjectDayStatsOutputDTO::date))
                .toList();

        List<CompanyRunningMemberOutputDTO> running = runningSessions.stream()
                .map(s -> {
                    TimeEntry entry = entryById.get(s.getEntryId());
                    return new CompanyRunningMemberOutputDTO(
                            entry.getUserId(),
                            userNames.getOrDefault(entry.getUserId(), ""),
                            entry.getId(),
                            entry.getName(),
                            s.getStartTime());
                })
                .toList();

        return new CompanyTimeEntryStatsOutputDTO(
                totalSeconds,
                entries.size(),
                running.size(),
                secondsByUser.size(),
                previousTotalSeconds,
                previousEntries.size(),
                members,
                projects,
                days,
                projectDays,
                running);
    }

    private List<TimeEntry> fetchEntries(UUID companyId, UUID actorId, CompanyTimeEntriesFilterInputDTO input,
            LocalDateTime from, LocalDateTime to, boolean fullScope) {
        TimeEntryFilter filter = input.toDomainFilter();
        filter.setCompanyId(companyId);
        filter.setFrom(from);
        filter.setTo(to);
        if (!fullScope) {
            filter.setLeadScopeUserId(actorId);
        }
        return timeEntryRepository.search(filter);
    }

    private long sumClampedSeconds(List<TimeEntrySession> sessions, LocalDateTime from, LocalDateTime to) {
        long total = 0;
        for (TimeEntrySession session : sessions) {
            LocalDateTime start = session.getStartTime() != null && session.getStartTime().isAfter(from)
                    ? session.getStartTime()
                    : from;
            LocalDateTime end = session.getEndTime() != null
                    ? (session.getEndTime().isBefore(to) ? session.getEndTime() : to)
                    : to;
            if (end.isAfter(start)) {
                total += Duration.between(start, end).getSeconds();
            }
        }
        return total;
    }

    private List<Project> searchProjectsByIds(java.util.Collection<UUID> projectIds) {
        ProjectFilter filter = ProjectFilterMapper.byIds(projectIds);
        return projectRepository.search(filter);
    }

    private Map<LocalDate, Long> zeroFillDays(LocalDate from, LocalDate to) {
        Map<LocalDate, Long> days = new LinkedHashMap<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            days.put(day, 0L);
        }
        return days;
    }
}
