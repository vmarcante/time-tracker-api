package com.vmarcante.time_tracker.core.application.user.session.use_cases;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import com.vmarcante.time_tracker.core.domain.user.session.repository.UserSessionRepository;

class DeleteExpiredSessionsUseCaseImplTest {

    @Test
    void whenExecuting_thenShouldDeleteExpiredSessions() {
        // Arrange
        UserSessionRepository userSessionRepository = mock(UserSessionRepository.class);
        DeleteExpiredSessionsUseCaseImpl useCase = new DeleteExpiredSessionsUseCaseImpl(userSessionRepository);

        // Act
        useCase.execute();

        // Assert
        verify(userSessionRepository).deleteExpiredSessions();
    }
}
