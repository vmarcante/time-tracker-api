package com.vmarcante.time_tracker.core.domain.timeentry.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntrySessionFilter;

public interface TimeEntrySessionRepository {

    TimeEntrySession save(TimeEntrySession session);

    Optional<TimeEntrySession> findOne(TimeEntrySessionFilter filter);

    List<TimeEntrySession> search(TimeEntrySessionFilter filter);

    List<TimeEntrySession> search(TimeEntrySessionFilter filter, Sort sort);

    Map<UUID, List<TimeEntrySession>> findActiveByEntryIds(Collection<UUID> entryIds);

    /**
     * Soma os segundos de sessão por dia (dia de início da sessão) para um
     * usuário. Sessões em curso contam até o momento da consulta.
     * Intervalo: [from, toExclusive).
     */
    Map<LocalDate, Long> sumSessionSecondsByDay(UUID userId, LocalDateTime from, LocalDateTime toExclusive);

    boolean exists(TimeEntrySessionFilter filter);
}
