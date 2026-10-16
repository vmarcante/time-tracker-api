package com.vmarcante.time_tracker.core.application.timeentry.session.policy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;

@Component
public class TimeEntrySessionValidationPolicy {

    public static final int MAX_PARALLEL_SESSIONS = 3;

    public void validateSessionTimes(LocalDateTime startTime, LocalDateTime endTime) throws ApplicationException {
        if (startTime == null) {
            throw new ApplicationException("timeentry.session.start.required", null);
        }
        if (endTime != null && !endTime.isAfter(startTime)) {
            throw new ApplicationException("timeentry.session.end.before.start", null);
        }
    }

    /**
     * Garante que o intervalo candidato não crie mais de {@value MAX_PARALLEL_SESSIONS}
     * sessões simultâneas na entry. Sessões em curso têm fim em aberto e contam como
     * concorrentes a qualquer intervalo posterior ao seu início.
     */
    public void validateMaxParallelSessions(
            List<TimeEntrySession> existingSessions,
            UUID excludedSessionId,
            LocalDateTime startTime,
            LocalDateTime endTime) throws ApplicationException {
        List<LocalDateTime> starts = new ArrayList<>();
        List<LocalDateTime> ends = new ArrayList<>();

        for (TimeEntrySession session : existingSessions) {
            if (session.getId() != null && session.getId().equals(excludedSessionId)) {
                continue;
            }
            starts.add(session.getStartTime());
            ends.add(session.getEndTime() != null ? session.getEndTime() : LocalDateTime.MAX);
        }
        starts.add(startTime);
        ends.add(endTime != null ? endTime : LocalDateTime.MAX);

        starts.sort(null);
        ends.sort(null);

        int concurrent = 0;
        int i = 0;
        int j = 0;
        while (i < starts.size()) {
            if (j < ends.size() && !starts.get(i).isBefore(ends.get(j))) {
                concurrent--;
                j++;
                continue;
            }
            concurrent++;
            i++;
            if (concurrent > MAX_PARALLEL_SESSIONS) {
                throw new ApplicationException("timeentry.session.max.parallel", null);
            }
        }
    }
}
