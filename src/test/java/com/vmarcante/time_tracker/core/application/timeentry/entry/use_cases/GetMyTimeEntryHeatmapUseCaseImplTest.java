package com.vmarcante.time_tracker.core.application.timeentry.entry.use_cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.input.MyTimeEntryHeatmapInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.dto.output.TimeEntryDayStatsOutputDTO;
import com.vmarcante.time_tracker.core.domain.timeentry.repository.TimeEntrySessionRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

class GetMyTimeEntryHeatmapUseCaseImplTest {

    private TimeEntrySessionRepository sessionRepository;
    private SecurityContextPort securityContext;
    private GetMyTimeEntryHeatmapUseCaseImpl useCase;

    private static final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        sessionRepository = mock(TimeEntrySessionRepository.class);
        securityContext = mock(SecurityContextPort.class);
        useCase = new GetMyTimeEntryHeatmapUseCaseImpl(sessionRepository, securityContext);
    }

    private MyTimeEntryHeatmapInputDTO input(LocalDate from, LocalDate to) {
        MyTimeEntryHeatmapInputDTO input = new MyTimeEntryHeatmapInputDTO();
        input.setFrom(from);
        input.setTo(to);
        return input;
    }

    @Test
    void givenRangeWithData_whenExecuting_thenReturnsDaysSortedAscending() {
        // Arrange
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        when(securityContext.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(sessionRepository.sumSessionSecondsByDay(
                eq(USER_ID), eq(from.atStartOfDay()), eq(to.plusDays(1).atStartOfDay())))
                .thenReturn(Map.of(
                        LocalDate.of(2026, 10, 15), 7200L,
                        LocalDate.of(2026, 10, 2), 3600L));

        // Act
        List<TimeEntryDayStatsOutputDTO> result = useCase.execute(input(from, to));

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2026, 10, 2));
        assertThat(result.get(0).seconds()).isEqualTo(3600L);
        assertThat(result.get(1).date()).isEqualTo(LocalDate.of(2026, 10, 15));
        assertThat(result.get(1).seconds()).isEqualTo(7200L);
    }

    @Test
    void givenNoData_whenExecuting_thenReturnsEmptyList() {
        // Arrange
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        when(securityContext.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(sessionRepository.sumSessionSecondsByDay(
                any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Map.of());

        // Act
        List<TimeEntryDayStatsOutputDTO> result = useCase.execute(input(from, to));

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void givenInvertedRange_whenExecuting_thenThrows() {
        // Arrange
        when(securityContext.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        MyTimeEntryHeatmapInputDTO input = input(LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 1));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(input)).isInstanceOf(ApplicationException.class);
        verify(sessionRepository, never()).sumSessionSecondsByDay(
                any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void givenUnauthenticated_whenExecuting_thenThrows() {
        // Arrange
        when(securityContext.getCurrentUserId()).thenReturn(Optional.empty());
        MyTimeEntryHeatmapInputDTO input = input(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(input)).isInstanceOf(ApplicationException.class);
        verifyNoInteractions(sessionRepository);
    }
}
