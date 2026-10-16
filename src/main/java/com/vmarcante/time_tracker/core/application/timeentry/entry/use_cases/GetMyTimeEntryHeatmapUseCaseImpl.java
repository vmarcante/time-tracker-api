package com.vmarcante.time_tracker.core.application.timeentry.entry.use_cases;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.input.MyTimeEntryHeatmapInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.TimeEntryDayStatsOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.in.GetMyTimeEntryHeatmapUseCase;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class GetMyTimeEntryHeatmapUseCaseImpl implements GetMyTimeEntryHeatmapUseCase {

    private final TimeEntrySessionRepository sessionRepository;
    private final SecurityContextPort securityContext;

    public GetMyTimeEntryHeatmapUseCaseImpl(
            TimeEntrySessionRepository sessionRepository,
            SecurityContextPort securityContext) {
        this.sessionRepository = sessionRepository;
        this.securityContext = securityContext;
    }

    @Override
    public List<TimeEntryDayStatsOutputDTO> execute(MyTimeEntryHeatmapInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        LocalDate today = LocalDate.now();
        LocalDate from = input.getFrom() != null ? input.getFrom()
                : today.withDayOfMonth(1);
        LocalDate to = input.getTo() != null ? input.getTo()
                : today;
        if (to.isBefore(from)) {
            throw new ApplicationException("timeentry.period.invalid", null);
        }

        LocalDateTime fromInclusive = from.atStartOfDay();
        LocalDateTime toExclusive = to.plusDays(1).atStartOfDay();
        Map<LocalDate, Long> secondsByDay = sessionRepository.sumSessionSecondsByDay(
                currentUserId.get(), fromInclusive, toExclusive);

        return secondsByDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new TimeEntryDayStatsOutputDTO(entry.getKey(), entry.getValue()))
                .toList();
    }
}
