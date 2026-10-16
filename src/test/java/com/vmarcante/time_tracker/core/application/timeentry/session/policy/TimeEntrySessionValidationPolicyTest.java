package com.vmarcante.time_tracker.core.application.timeentry.session.policy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;

class TimeEntrySessionValidationPolicyTest {

    private final TimeEntrySessionValidationPolicy policy = new TimeEntrySessionValidationPolicy();

    @Test
    void givenNullStartTime_whenValidatingSessionTimes_thenThrowsRequired() {
        // Arrange
        LocalDateTime startTime = null;
        LocalDateTime endTime = LocalDateTime.now();

        // Act & Assert
        assertThatThrownBy(() -> policy.validateSessionTimes(startTime, endTime))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("timeentry.session.start.required");
    }

    @Test
    void givenEndBeforeStart_whenValidatingSessionTimes_thenThrowsEndBeforeStart() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2026, 10, 16, 14, 0);
        LocalDateTime endTime = startTime.minusMinutes(5);

        // Act & Assert
        assertThatThrownBy(() -> policy.validateSessionTimes(startTime, endTime))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("timeentry.session.end.before.start");
    }

    @Test
    void givenTwoOverlappingSessions_whenAddingThird_thenAllows() {
        // Arrange
        LocalDateTime base = LocalDateTime.of(2026, 10, 16, 14, 0);
        TimeEntrySession first = session(base, base.plusHours(1));
        TimeEntrySession second = session(base.plusMinutes(30), base.plusHours(2));

        LocalDateTime newStart = base.plusMinutes(45);
        LocalDateTime newEnd = base.plusHours(1).plusMinutes(30);

        // Act & Assert
        assertThatCode(() -> policy.validateMaxParallelSessions(
                List.of(first, second), null, newStart, newEnd))
                .doesNotThrowAnyException();
    }

    @Test
    void givenThreeOverlappingSessions_whenAddingFourth_thenThrowsMaxParallel() {
        // Arrange
        LocalDateTime base = LocalDateTime.of(2026, 10, 16, 14, 0);
        TimeEntrySession first = session(base, base.plusHours(2));
        TimeEntrySession second = session(base.plusMinutes(10), base.plusHours(1));
        TimeEntrySession third = session(base.plusMinutes(20), base.plusMinutes(50));

        LocalDateTime newStart = base.plusMinutes(25);
        LocalDateTime newEnd = base.plusMinutes(40);

        // Act & Assert
        assertThatThrownBy(() -> policy.validateMaxParallelSessions(
                List.of(first, second, third), null, newStart, newEnd))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("timeentry.session.max.parallel");
    }

    @Test
    void givenThreeOverlappingSessions_whenEditingOneOfThem_thenExcludesItFromCount() {
        // Arrange
        LocalDateTime base = LocalDateTime.of(2026, 10, 16, 14, 0);
        UUID editedId = UUID.randomUUID();
        TimeEntrySession edited = session(base, base.plusMinutes(30));
        edited.setId(editedId);
        TimeEntrySession second = session(base.plusMinutes(10), base.plusHours(1));
        TimeEntrySession third = session(base.plusMinutes(20), base.plusMinutes(50));

        LocalDateTime newStart = base.plusMinutes(25);
        LocalDateTime newEnd = base.plusMinutes(40);

        // Act & Assert — a sessão editada sai da conta, ficam 2 + ela = 3
        assertThatCode(() -> policy.validateMaxParallelSessions(
                List.of(edited, second, third), editedId, newStart, newEnd))
                .doesNotThrowAnyException();
    }

    @Test
    void givenNonOverlappingSessions_whenAdding_thenAllows() {
        // Arrange
        LocalDateTime base = LocalDateTime.of(2026, 10, 16, 14, 0);
        TimeEntrySession first = session(base, base.plusMinutes(30));
        TimeEntrySession second = session(base.plusHours(1), base.plusHours(2));
        TimeEntrySession third = session(base.plusHours(2), base.plusHours(3));

        LocalDateTime newStart = base.plusHours(3);
        LocalDateTime newEnd = base.plusHours(4);

        // Act & Assert
        assertThatCode(() -> policy.validateMaxParallelSessions(
                List.of(first, second, third), null, newStart, newEnd))
                .doesNotThrowAnyException();
    }

    private TimeEntrySession session(LocalDateTime startTime, LocalDateTime endTime) {
        TimeEntrySession session = new TimeEntrySession();
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        return session;
    }
}
